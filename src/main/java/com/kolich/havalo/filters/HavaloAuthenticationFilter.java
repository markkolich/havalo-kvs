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

package com.kolich.havalo.filters;

import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.handlers.requests.CuracaoContext;
import com.kolich.curacao.handlers.requests.filters.CuracaoRequestFilter;
import com.kolich.havalo.components.RepositoryManagerComponent;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.authentication.AuthenticationException;
import com.kolich.havalo.exceptions.authentication.BadCredentialsException;
import org.slf4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static com.google.common.net.HttpHeaders.*;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;
import static org.slf4j.LoggerFactory.getLogger;

public final class HavaloAuthenticationFilter implements CuracaoRequestFilter {

	private static final Logger logger__ =
        getLogger(HavaloAuthenticationFilter.class);

	public static final String HAVALO_AUTHENTICATION_ATTRIBUTE =
        "havalo.authentication";

	private static final String HAVALO_AUTHORIZATION_PREFIX = "Havalo ";
	private static final String HAVALO_AUTHORIZATION_SEPARATOR = ":";

	private final HavaloUserService userService_;

    @Injectable
	public HavaloAuthenticationFilter(final RepositoryManagerComponent component) {
		userService_ = new HavaloUserService(component.getRepositoryManager());
	}

    @Override
    public final void filter(final CuracaoContext context) throws Exception {
        try {
            final HttpServletRequest request = context.request_;
            // Extract the Authorization header from the incoming HTTP request.
            String header = request.getHeader(AUTHORIZATION);
            // If the header does not exist or does not start with the correct
            // token, give up immediately.
            if(header == null || !header.startsWith(HAVALO_AUTHORIZATION_PREFIX)) {
                throw new AuthenticationException("Request did not contain " +
                    "a valid '" + AUTHORIZATION + "' header.");
            }
            // Extract just the part of the Authorization header that follows
            // the Havalo authorization prefix.
            header = header.substring(HAVALO_AUTHORIZATION_PREFIX.length());
            final String[] tokens = header.split(HAVALO_AUTHORIZATION_SEPARATOR, 2);
            if(tokens == null || tokens.length != 2) {
                throw new AuthenticationException("Failed to extract correct " +
                    "number of tokens from '" + AUTHORIZATION + "' header.");
            }
            // If we get here, then we must have had some valid input
            // Authorization header with a real access key and signature.
            final String accessKey = tokens[0], signature = tokens[1];
            // request.getRequestURI();
            // Extract username from incoming signed request header.
            // Expected format is ...
            //    Authorization: Havalo AccessKey:Signature
            // ... where the AccessKey is the unique UUID used to identify the user.
            // And, the Signature is ...
            //    Base64( HMAC-SHA256( UTF-8-Encoding-Of( AccessSecret, StringToSign ) ) );
            // And, the StringToSign is ....
            //    HTTP-Verb (GET, PUT, POST, or DELETE) + "\n" +
            //    RFC822 Date (from 'Date' request header, must exist) + "\n" +
            //    Content-Type (from 'Content-Type' request header, optional) + "\n" +
            //    CanonicalizedResource (the part of this request's URL from
            //        the protocol name up to the query string in the first line
            //        of the HTTP request)
            // Call the user details service to load the user data for the UUID.
            final KeyPair userKp = userService_.loadKeyPairById(
                UUID.fromString(accessKey));
            if(userKp == null) {
                throw new AuthenticationException("User service returned " +
                    "null, which is an interface contract violation.");
            }
            // Get the string to sign -- will fail gracefully if the incoming
            // request does not have the proper headers attached to it.
            final String stringToSign = getStringToSign(request);
            // Compute the resulting signed signature.
            final String computed = HMACSHA256Signer.sign(userKp, stringToSign);
            // Does the signature match what was passed to us in the
            // Authorization request header?
            if(!computed.equals(signature)) {
                throw new BadCredentialsException("Signatures did not " +
                    "match (request=" + signature + ", computed=" + computed +
                    ")");
            }
            // Success!
            request.setAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE, userKp);
        } catch (Exception e) {
            logger__.debug("Authentication failure; service failed " +
                "to authenticate request.", e);
            throw new AuthenticationException("Authentication " +
                "failed; either the provided signature did not match, " +
                "or you do not have permission to access the requested " +
                "resource.", e);
        }
    }
	
	private static final String getStringToSign(final HttpServletRequest request) {
		final StringBuilder sb = new StringBuilder();
		// HTTP-Verb (GET, PUT, POST, or DELETE) + "\n"
		sb.append(request.getMethod().toUpperCase()).append(LINE_SEPARATOR_UNIX);
		// RFC822 Date (from 'Date' request header, must exist) + "\n" +
		final String dateHeader;
		if((dateHeader = request.getHeader(DATE)) == null) {
			throw new BadCredentialsException("Incoming request missing " +
				"required '" + DATE + "' request header.");
		}
		sb.append(dateHeader).append(LINE_SEPARATOR_UNIX);
		// Content-Type (from 'Content-Type' request header, optional) + "\n" +
		final String contentType;
		if((contentType = request.getHeader(CONTENT_TYPE)) != null) {
			sb.append(contentType);
		}
		sb.append(LINE_SEPARATOR_UNIX);
		// CanonicalizedResource
		sb.append(request.getRequestURI());
		return sb.toString();
	}

    /**
	 * Computes an HMAC-SHA256 signature.
	 */
	private static final class HMACSHA256Signer {
		
		private static final String HMAC_SHA256_ALGORITHM_NAME = "HmacSHA256";
			
		/**
	     * Returns a Base-64 encoded HMAC-SHA256 signature.
	     */
		public static final String sign(final KeyPair kp,
                                        final String input) {
			try {
				// Get a new instance of the HMAC-SHA256 algorithm.
				final Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM_NAME);
				// Init it with our secret and the secret-key algorithm.
				mac.init(new SecretKeySpec(getBytesUtf8(kp.getSecret()),
					HMAC_SHA256_ALGORITHM_NAME));
				// Actually sign the input.
				return newStringUtf8(encodeBase64(mac.doFinal(
					getBytesUtf8(input))));
			} catch (Exception e) {
				throw new AuthenticationException("Failed to SHA-256 " +
					"sign input string: " + input, e);
			}
		}
		
	}

}
