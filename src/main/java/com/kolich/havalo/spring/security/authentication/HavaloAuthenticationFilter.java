/**
 * Copyright (c) 2012 Mark S. Kolich
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

package com.kolich.havalo.spring.security.authentication;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.HttpHeaders.DATE;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;

import java.io.IOException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

public class HavaloAuthenticationFilter extends GenericFilterBean
	implements MessageSourceAware, InitializingBean {
	
	private static final String HTTP_AUTHORIZATION_HEADER = "Authorization";
	
	private static final String HAVALO_AUTHORIZATION_PREFIX = "Havalo ";
	private static final String HAVALO_AUTHORIZATION_SEPARATOR = ":";
	
	private MessageSourceAccessor messages_;
	
	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource_;
	
	private AuthenticationEntryPoint authenticationEntryPoint_;
	private UserDetailsService userDetailsService_;
		
	@Override
	public void afterPropertiesSet() throws ServletException {
		super.afterPropertiesSet();
		messages_ = SpringSecurityMessageSource.getAccessor();
		authenticationDetailsSource_ = new WebAuthenticationDetailsSource();
		checkNotNull(userDetailsService_, "A UserDetailsService is required.");
		checkNotNull(authenticationEntryPoint_, "An AuthenticationEntryPoint " +
			"is required.");
	}

	@Override
	public void doFilter(final ServletRequest req,
		final ServletResponse res, final FilterChain chain)
		throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        // Extract the Authorization header from the incoming HTTP request.
        String header = request.getHeader(HTTP_AUTHORIZATION_HEADER);
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
        	final UserDetails user = userDetailsService_.loadUserByUsername(
        		accessKey);
        	if(user == null) {
        		throw new AuthenticationServiceException("UserDetailsService " +
        			"returned null, which is an interface contract violation.");
        	}
        	// Get the string to sign -- will fail gracefully if the incoming
        	// request does not have the proper headers attached to it.
        	final String stringToSign = getStringToSign(request);
        	// Compute the resulting signed signature.
        	final String computed = HMACSHA256Signer.sign(user, stringToSign);
        	// Does the signature match what was passed to us in the
        	// Authorization request header?
        	if(!computed.equals(signature)) {
        		throw new BadCredentialsException("Signatures did not " +
        			"match (request=" + signature + ", computed=" + computed +
        				")");
        	}
        	// Successful authentication!
            SecurityContextHolder.getContext().setAuthentication(
            	success(request, user));
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
	
	private Authentication success(final HttpServletRequest request,
		final UserDetails user) {
        final UsernamePasswordAuthenticationToken authRequest =
        	new UsernamePasswordAuthenticationToken(user, user.getPassword());
        authRequest.setDetails(authenticationDetailsSource_.buildDetails(
        	request));
        return authRequest;
    }
	
	private void fail(final HttpServletRequest request,
		final HttpServletResponse response,
		final AuthenticationException failed)
		throws IOException, ServletException {
		// Invalidate the security context, if any.
        SecurityContextHolder.getContext().setAuthentication(null);
        // Commence a new security authentication scheme and commence.
        authenticationEntryPoint_.commence(request, response, failed);
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
	
	@Override
	public void setMessageSource(MessageSource messageSource) {
		messages_ = new MessageSourceAccessor(messageSource);
	}
	
	public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
		authenticationEntryPoint_ = authenticationEntryPoint;
    }
	
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		userDetailsService_ = userDetailsService;
    }
	
	/**
	 * Computes an HMAC-SHA256 signature.
	 */
	private static final class HMACSHA256Signer {
		
		private static final String HMAC_SHA256_ALGORITHM_NAME = "HmacSHA256";
			
		/**
	     * Returns a Base-64 encoded HMAC-SHA256 signature.
	     */
		public static final String sign(final UserDetails details,
			final String input) {
			try {
				// Get a new instance of the HMAC-SHA256 algorithm.
				final Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM_NAME);
				// Init it with our secret and the secret-key algorithm.
				mac.init(new SecretKeySpec(getBytesUtf8(details.getPassword()),
					HMAC_SHA256_ALGORITHM_NAME));
				// Actually sign the input.
				return newStringUtf8(encodeBase64(mac.doFinal(
					getBytesUtf8(input))));
			} catch (Exception e) {
				throw new AuthenticationServiceException("Failed to SHA-256 " +
					"sign input string: " + input, e);
			}
		}
		
	}
	
}
