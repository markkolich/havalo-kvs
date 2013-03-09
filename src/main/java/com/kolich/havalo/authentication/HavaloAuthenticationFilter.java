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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.exceptions.authentication.AuthenticationException;
import com.kolich.havalo.exceptions.authentication.BadCredentialsException;
import com.kolich.havalo.exceptions.authentication.UsernameNotFoundException;

public final class HavaloAuthenticationFilter implements Filter {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloAuthenticationFilter.class);
		
	private static final String HAVALO_AUTHORIZATION_PREFIX = "Havalo ";
	private static final String HAVALO_AUTHORIZATION_SEPARATOR = ":";
	
	private HavaloUserService userService_;
	
	@Override
	public void init(final FilterConfig config) throws ServletException {
		logger__.info("In init()");
		userService_ = (HavaloUserService)config.getServletContext()
			.getAttribute(HAVALO_USER_SERVICE_ATTRIBUTE);
	}
	
	@Override
	public void destroy() {
		logger__.info("In destroy()");
	}
	
	@Override
	public void doFilter(final ServletRequest req,
		final ServletResponse res, final FilterChain chain)
		throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        // Extract the Authorization header from the incoming HTTP request.
        String header = request.getHeader(AUTHORIZATION);
        // If the header does not exist or does not start with the correct
        // token, give up immeaditely.
        if(header == null || !header.startsWith(HAVALO_AUTHORIZATION_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        // Extract just the part of the Authorization header that follows
        // the Havalo authorization prefix.
        header = header.substring(HAVALO_AUTHORIZATION_PREFIX.length());
        final String[] tokens = header.split(HAVALO_AUTHORIZATION_SEPARATOR, 2);
        if(tokens == null || tokens.length != 2) {
        	chain.doFilter(request, response);
            return;
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
        try {
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
        	// Successful authentication!
        	request.setAttribute(arg0, arg1);
            chain.doFilter(request, response);
        } catch (UsernameNotFoundException notFound) {
            fail(request, response,
            	new BadCredentialsException(messages_.getMessage(
            		"HavaloAuthenticationFilter.usernameNotFound",
            			new Object[]{accessKey}, "Havalo UUID {0} not found"),
            				notFound));
        } catch (BadCredentialsException badCreds) {
        	fail(request, response,
            	new BadCredentialsException(messages_.getMessage(
            		"HavaloAuthenticationFilter.badCredentials",
            			new Object[]{accessKey}, "Bad access credentials " +
            				"for ID {0}"), badCreds));
        } catch (Exception e) {
        	fail(request, response,
            	new AuthenticationServiceException(messages_.getMessage(
            		"HavaloAuthenticationFilter.authServicesUnavailable",
            			new Object[]{accessKey}, "Havalo authentication " +
            				"service unavailable for auth: {0}"), e));
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
