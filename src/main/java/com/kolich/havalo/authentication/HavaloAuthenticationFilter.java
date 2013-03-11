package com.kolich.havalo.authentication;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.HttpHeaders.DATE;
import static com.kolich.havalo.HavaloServletContextBootstrap.HAVALO_USER_SERVICE_ATTRIBUTE;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.authentication.AuthenticationException;
import com.kolich.havalo.exceptions.authentication.BadCredentialsException;
import com.kolich.havalo.exceptions.authentication.UsernameNotFoundException;

public final class HavaloAuthenticationFilter implements Filter {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloAuthenticationFilter.class);
	
	public static final String HAVALO_AUTHENTICATION_ATTRIBUTE = "havalo.authentication";
		
	private static final String HAVALO_AUTHORIZATION_PREFIX = "Havalo ";
	private static final String HAVALO_AUTHORIZATION_SEPARATOR = ":";
	
	private HavaloUserService userService_;
	
	private ExecutorService pool_;
	
	@Override
	public void init(final FilterConfig fConfig) throws ServletException {
		logger__.info("In init()");
		final ServletContext context = fConfig.getServletContext();		
		userService_ = (HavaloUserService)context.getAttribute(HAVALO_USER_SERVICE_ATTRIBUTE);
		pool_ = Executors.newCachedThreadPool(
			new ThreadFactoryBuilder()
				.setDaemon(true)
				.setPriority(Thread.MAX_PRIORITY)
				.setNameFormat("havalo-async-auth-filter-%d")
				.build());
	}
	
	@Override
	public void destroy() {
		logger__.info("In destroy()");
	}
	
	@Override
	public void doFilter(final ServletRequest request,
		final ServletResponse response, final FilterChain chain)
		throws IOException, ServletException {
		final AsyncContext context = request.startAsync(request, response);
		final HavaloAuthorizationAsyncListener listener =
			new HavaloAuthorizationAsyncListener(context, chain, userService_);
		context.addListener(listener);
		pool_.submit(listener);
	}
	
	private static final class HavaloAuthorizationAsyncListener
		implements Callable<Void>, AsyncListener {
		
		private final AsyncContext context_;
		private final HttpServletRequest request_;
		private final HttpServletResponse response_;
		private final FilterChain chain_;
		
		private final HavaloUserService userService_;
		
		private boolean authSuccess_ = false;
		
		private HavaloAuthorizationAsyncListener(final AsyncContext context,
			final FilterChain chain, final HavaloUserService userService) {
			context_ = context;
			request_ = (HttpServletRequest) context_.getRequest();
	        response_ = (HttpServletResponse) context_.getResponse();
			chain_ = chain;
			userService_ = userService;
		}
		
		@Override
		public Void call() throws Exception {			
	        try {
	        	// Extract the Authorization header from the incoming HTTP request.
	            String header = request_.getHeader(AUTHORIZATION);
	            // If the header does not exist or does not start with the correct
	            // token, give up immeaditely.
	            if(header == null || !header.startsWith(HAVALO_AUTHORIZATION_PREFIX)) {
	            	logger__.info("no authorization header");
	                return null;
	            }
	            // Extract just the part of the Authorization header that follows
	            // the Havalo authorization prefix.
	            header = header.substring(HAVALO_AUTHORIZATION_PREFIX.length());
	            final String[] tokens = header.split(HAVALO_AUTHORIZATION_SEPARATOR, 2);
	            if(tokens == null || tokens.length != 2) {
	            	logger__.info("no tokens or token length didn't match.");
	            	return null;
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
	        	final String stringToSign = getStringToSign(request_);
	        	// Compute the resulting signed signature.
	        	final String computed = HMACSHA256Signer.sign(userKp, stringToSign);
	        	// Does the signature match what was passed to us in the
	        	// Authorization request header?
	        	if(!computed.equals(signature)) {
	        		throw new BadCredentialsException("Signatures did not " +
	        			"match (request=" + signature + ", computed=" + computed +
	        				")");
	        	}
	        	// Successful authentication!
	        	request_.setAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE, userKp);
	        	authSuccess_ = true;
	        } catch (UsernameNotFoundException e) {
	        	logger__.info("username not found", e);
	        	// TODO need to do something better
	        } catch (BadCredentialsException e) {
	        	logger__.info("bad credentials", e);
	        	// TODO need to do something better
	        } catch (Exception e) {
	        	logger__.info("auth service failure", e);
	        	// TODO need to do something better      	
	        } finally {
	        	context_.complete();
	        }
	        return null; // Meh, for Void return type
		}
		
		@Override
		public void onStartAsync(final AsyncEvent event) throws IOException {
			// Nothing, intentional.
		}
		
		@Override
		public void onComplete(final AsyncEvent event) throws IOException {
			try {
				if(authSuccess_) {
	        		chain_.doFilter(request_, response_);
	        	} else {
	        		response_.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	        	}
			} catch (IOException e) {
				// TODO what to do here?
			} catch (Exception e) {
				// TODO what to do here?
			}
		}
		
		@Override
		public void onError(final AsyncEvent event) throws IOException {
			onComplete(event);
		}
		
		@Override
		public void onTimeout(final AsyncEvent event) throws IOException {
			onComplete(event);
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
				"required " + DATE + " HTTP header.");
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
		public static final String sign(final KeyPair kp, final String input) {
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
