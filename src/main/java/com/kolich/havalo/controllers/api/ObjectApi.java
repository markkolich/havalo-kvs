/**
 * Copyright (c) 2015 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.havalo.controllers.api;

import com.kolich.bolt.ReentrantReadWriteEntityLock;
import com.kolich.common.util.secure.KolichChecksum;
import curacao.annotations.Controller;
import curacao.annotations.Injectable;
import curacao.annotations.RequestMapping;
import curacao.annotations.parameters.convenience.ContentLength;
import curacao.annotations.parameters.convenience.ContentType;
import curacao.annotations.parameters.convenience.IfMatch;
import curacao.entities.CuracaoEntity;
import curacao.entities.empty.StatusCodeOnlyCuracaoEntity;
import curacao.mappers.request.matchers.CuracaoAntPathMatcher;
import com.kolich.havalo.components.RepositoryManagerComponent;
import com.kolich.havalo.controllers.HavaloApiController;
import com.kolich.havalo.entities.types.DiskObject;
import com.kolich.havalo.entities.types.HashedFileObject;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.entities.types.Repository;
import com.kolich.havalo.exceptions.objects.ObjectConflictException;
import com.kolich.havalo.exceptions.objects.ObjectLengthNotSpecifiedException;
import com.kolich.havalo.exceptions.objects.ObjectNotFoundException;
import com.kolich.havalo.exceptions.objects.ObjectTooLargeException;
import com.kolich.havalo.filters.HavaloAuthenticationFilter;
import com.kolich.havalo.mappers.ObjectKeyArgumentMapper.ObjectKey;
import org.slf4j.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Files.move;
import static com.google.common.net.HttpHeaders.*;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static com.kolich.common.util.secure.KolichChecksum.getSHA1HashAndCopy;
import static curacao.annotations.RequestMapping.Method.*;
import static com.kolich.havalo.HavaloConfigurationFactory.getMaxUploadSize;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
public class ObjectApi extends HavaloApiController {

    private static final Logger logger__ = getLogger(ObjectApi.class);

    private static final String OCTET_STREAM_TYPE = OCTET_STREAM.toString();

    private final long uploadMaxSize_;

    @Injectable
    public ObjectApi(final RepositoryManagerComponent component) {
        super(component.getRepositoryManager());
        uploadMaxSize_ = getMaxUploadSize();
    }

    @RequestMapping(methods=HEAD,
                    value="/api/object/{key}",
                    matcher=CuracaoAntPathMatcher.class,
                    filters=HavaloAuthenticationFilter.class)
    public final void head(final ObjectKey key,
                           final KeyPair userKp,
                           final HttpServletResponse response,
                           final AsyncContext context) throws Exception {
        final Repository repo = getRepository(userKp.getKey());
        new ReentrantReadWriteEntityLock<Void>(repo) {
            @Override
            public Void transaction() throws Exception {
                final HashedFileObject hfo = getHashedFileObject(repo,
                    // The URL-decoded key of the object to delete.
                    key,
                    // Fail if not found.
                    true);
                new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
                    @Override
                    public HashedFileObject transaction() throws Exception {
                        final DiskObject object = getCanonicalObject(repo, hfo);
                        streamHeaders(object, hfo, response);
                        return hfo;
                    }
                }.read(); // Shared read lock on file object
                return null;
            }
            @Override
            public void success(final Void v) throws Exception {
                context.complete();
            }
        }.read(); // Shared read lock on repo
    }

    @RequestMapping(methods=GET,
                    value="/api/object/{key}",
                    matcher=CuracaoAntPathMatcher.class,
                    filters=HavaloAuthenticationFilter.class)
    public final void get(final ObjectKey key,
                          final KeyPair userKp,
                          final HttpServletResponse response,
                          final AsyncContext context) throws Exception {
        final Repository repo = getRepository(userKp.getKey());
        new ReentrantReadWriteEntityLock<Void>(repo) {
            @Override
            public Void transaction() throws Exception {
                final HashedFileObject hfo = getHashedFileObject(repo,
                    // The URL-decoded key of the object to delete.
                    key,
                    // Fail if not found.
                    true);
                new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
                    @Override
                    public HashedFileObject transaction() throws Exception {
                        final DiskObject object = getCanonicalObject(repo, hfo);
                        // Validate that the object file exists on disk
                        // before we attempt to load it.
                        if(!object.getFile().exists()) {
                            throw new ObjectNotFoundException("Failed " +
                                "to find canonical object on disk " +
                                "(key=" + key + ", file=" +
                                object.getFile().getAbsolutePath() +
                                ")");
                        }
                        streamHeaders(object, hfo, response);
                        streamObject(object, response);
                        return hfo;
                    }
                }.read(); // Shared read lock on file object, wait
                return null;
            }
            @Override
            public void success(final Void v) throws Exception {
                context.complete();
            }
        }.read(false); // Shared read lock on repo, no wait
    }

    @RequestMapping(methods=PUT,
                    value="/api/object/{key}",
                    matcher=CuracaoAntPathMatcher.class,
                    filters=HavaloAuthenticationFilter.class)
    public final HashedFileObject put(final ObjectKey key,
                                      final KeyPair userKp,
                                      @IfMatch final String ifMatch,
                                      @ContentType final String contentType,
                                      @ContentLength final Long contentLength,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {
        final Repository repo = getRepository(userKp.getKey());
        return new ReentrantReadWriteEntityLock<HashedFileObject>(repo) {
            @Override
            public HashedFileObject transaction() throws Exception {
                // Havalo requires the consumer to send a Content-Length
                // request header with the request when uploading an
                // object.
                if(contentLength < 0L) {
                    // A value of -1 indicates that no Content-Length
                    // header was set.  Bail gracefully.
                    throw new ObjectLengthNotSpecifiedException("Client " +
                        "sent request where '" + CONTENT_LENGTH +
                        "' header was not present or less than zero.");
                }
                // Only accept the object if the Content-Length of the
                // incoming request is less than or equal to the max
                // upload size.
                if(contentLength > uploadMaxSize_) {
                    throw new ObjectTooLargeException("The '" +
                        CONTENT_LENGTH + "' of the incoming request " +
                        "is too large. Max upload size allowed is " +
                        uploadMaxSize_ + "-bytes.");
                }
                final HashedFileObject hfo = getHashedFileObject(repo, key);
                return new ReentrantReadWriteEntityLock<HashedFileObject>(hfo) {
                    @Override
                    public HashedFileObject transaction() throws Exception {
                        final String eTag = hfo.getFirstHeader(ETAG);
                        // If we have an incoming If-Match, we need to compare
                        // that against the current HFO before we attempt to
                        // update.  If the If-Match ETag does not match, fail.
                        if(ifMatch != null && eTag != null) {
                            // OK, we have an incoming If-Match ETag, use it.
                            // NOTE: HFO's will _always_ have an ETag attached
                            // to their meta-data.  ETag's are always computed
                            // for HFO's upload. But new HFO's (one's the repo
                            // have never seen before) may not yet have an ETag.
                            if(!ifMatch.equals(eTag)) {
                                throw new ObjectConflictException("Failed " +
                                    "to update HFO; incoming If-Match ETag " +
                                    "does not match (hfo=" + hfo.getName() +
                                    ", etag=" + eTag + ", if-match=" +
                                    ifMatch + ")");
                            }
                        }
                        final DiskObject object = getCanonicalObject(
                            repo, hfo,
                            // Create the File on disk if it does not
                            // already exist. Yay!
                            true);
                        // The file itself (should exist now).
                        final File objFile = object.getFile();
                        final File tempObjFile = object.getTempFile();
                        try(final InputStream is = request.getInputStream();
                            final OutputStream os = new FileOutputStream(tempObjFile);) {
                            // Compute the ETag (an MD5 hash of the file) while
                            // copying the file into place.  The bytes of the
                            // input stream and piped into an MD5 digest _and_
                            // to the output stream -- ideally computing the
                            // hash and copying the file at the same time.
                            // Set the resulting ETag header (meta data).
                            hfo.setETag(getSHA1HashAndCopy(is, os,
                                // Only copy as much as the incoming
                                // Content-Length header sez is going
                                // to be sent.  Anything more than this
                                // is caught gracefully and dropped.
                                contentLength));
                            // Move the uploaded file into place (moves
                            // the file from the temp location to the
                            // real destination inside of the repository
                            // on disk).
                            move(tempObjFile, objFile);
                            // Set the Last-Modified header (meta data).
                            hfo.setLastModified(objFile.lastModified());
                            // Set the Content-Length header (meta data).
                            hfo.setContentLength(objFile.length());
                            // Set the Content-Type header (meta data).
                            if(contentType != null) {
                                hfo.setContentType(contentType);
                            }
                        } catch (KolichChecksum.KolichChecksumException e) {
                            // Quietly delete the object on disk when
                            // it has exceeded the max upload size allowed
                            // by this Havalo instance.
                            throw new ObjectTooLargeException("The " +
                                "size of the incoming object is too " +
                                "large. Max upload size is " +
                                uploadMaxSize_ + "-bytes.", e);
                        } finally {
                            // Delete the file from the temp upload
                            // location.  Note, this file may not exist
                            // if the upload was successful and the object
                            // was moved into place.. which is OK here.
                            deleteQuietly(tempObjFile);
                        }
                        // Append an ETag header to the response for the
                        // PUT'ed object.
                        response.setHeader(ETAG, hfo.getFirstHeader(ETAG));
                        return hfo;
                    }
                    @Override
                    public void success(final HashedFileObject e) throws Exception {
                        // On success only, ask the repo manager to
                        // asynchronously flush this repository's meta
                        // data to disk.
                        flushRepository(repo);
                    }
                }.write(); // Exclusive lock on this HFO, no wait
            }
        }.read(false); // Shared read lock on repo, no wait
    }

    @RequestMapping(methods=DELETE,
                    value="/api/object/{key}",
                    matcher=CuracaoAntPathMatcher.class,
                    filters=HavaloAuthenticationFilter.class)
    public final CuracaoEntity delete(final ObjectKey key,
                                      @IfMatch final String ifMatch,
                                      final KeyPair userKp) throws Exception {
        // The delete operation does return a pointer to the "deleted"
        // HFO, but we're not using it, we're just dropping it on the
        // floor (intentionally not returning it to the caller).
        deleteHashedFileObject(userKp.getKey(),
            // The URL-decoded key of the object to delete.
            key,
            // Only delete the object if the provided ETag via the
            // If-Match header matches the object on disk.
            ifMatch);
        return new StatusCodeOnlyCuracaoEntity(SC_NO_CONTENT);
    }

    private static final void streamHeaders(final DiskObject object,
                                            final HashedFileObject hfo,
                                            final HttpServletResponse response) {
        checkNotNull(hfo, "Hashed file object cannot be null.");
        // Validate that the File object still exists.
        if(!object.getFile().exists()) {
            throw new ObjectNotFoundException("Object not " +
                "found (file=" + object.getFile().getAbsolutePath() +
                ", key=" + hfo.getName() + ")");
        }
        // Extract any response headers from this objects' meta data.
        final Map<String,List<String>> headers = hfo.getHeaders();
        // Always set the Content-Length header to the actual length of the
        // file on disk -- effectively overriding any "Content-Length" meta
        // data header set by the user in the PUT request.
        headers.put(CONTENT_LENGTH, Arrays.asList(Long.toString(object.getFile().length())));
        // Set the Content-Type header to a default if one was not set by
        // the consumer in the meta data.
        if(headers.get(CONTENT_TYPE) == null) {
            headers.put(CONTENT_TYPE, Arrays.asList(OCTET_STREAM_TYPE));
        }
        // Now, send all headers to the response stream.
        for(final Map.Entry<String,List<String>> entry : headers.entrySet()) {
            final String key = entry.getKey();
            for(final String value : entry.getValue()) {
                response.addHeader(key, value);
            }
        }
    }

    private static final void streamObject(final DiskObject object,
                                           final HttpServletResponse response) {
        try(final InputStream is = new FileInputStream(object.getFile());
            final OutputStream os = response.getOutputStream()) {
            copyLarge(is, os);
        } catch (Exception e) {
            // On any Exception case, just log the failure and move on.
            // We're closing the output stream in the finally{} block below
            // so it's not like we can fail here then somehow return an error
            // message to the API consumer.  We're handling this as gracefully
            // as best we can.
            logger__.error("Failed to stream object to client.", e);
        }
    }

}
