package com.kolich.havalo.authentication;

import java.io.IOException;

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

public class HavaloAuthenticationFilter implements Filter {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(HavaloAuthenticationFilter.class);
	
	private static final String HTTP_AUTHORIZATION_HEADER = "Authorization";
	
	private static final String HAVALO_AUTHORIZATION_PREFIX = "Havalo ";
	private static final String HAVALO_AUTHORIZATION_SEPARATOR = ":";
	
	@Override
	public void init(final FilterConfig config) throws ServletException {
		logger__.info("In init()");
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

}
