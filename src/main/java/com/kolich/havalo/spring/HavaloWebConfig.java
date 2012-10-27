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

package com.kolich.havalo.spring;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.kolich.havalo.spring.resolvers.HavaloUUIDMethodArgumentResolver;

@Configuration
public class HavaloWebConfig extends WebMvcConfigurationSupport {

	@Override
	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		final RequestMappingHandlerMapping hm = super.requestMappingHandlerMapping();
		// NOTE: It's important that this is set to false such that Spring
		// does not URL-decode the path on an incoming request before it
		// attempts to map it to the right Controller.  For example, if the
		// original incoming request is ...
		//   /havalo/app/api/object/files%2Fimag%252Fes%2Fbogus.jpg
		// ... then Spring, by defualt, will URL-decode this to ...
		//   /havalo/app/api/object/files/imag%2Fes/bogus.jpg
		// ... which is WRONG (it won't map to a real Controller because of
		// the URL-decoded slashes).
		hm.setUrlDecode(false);
		return hm;
	}
	
	/**
	 * Register a custom argument resolver, which is needed for our
	 * controllers that contain methods with UUID arguments.  This argument
	 * resolver extracts the UUID from the authenticated Principal if it
	 * exists and lets the controllers access the UUID as an argument into
	 * methods annotated with {@link RequestMapping}.
	 */
	@Override
	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		final RequestMappingHandlerAdapter mha = super.requestMappingHandlerAdapter();
		final HandlerMethodArgumentResolver hmar = new HavaloUUIDMethodArgumentResolver();
		mha.setCustomArgumentResolvers(Arrays.asList(hmar));
		return mha;
	}
	
}
