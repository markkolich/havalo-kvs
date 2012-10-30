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

package com.kolich.havalo.spring.controllers.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.kolich.havalo.spring.controllers.AbstractHavaloController;
import com.kolich.spring.controllers.KolichControllerClosure;

@Controller
@RequestMapping(value="/api")
public class Index extends AbstractHavaloController {
	
	private static final Logger logger__ = LoggerFactory.getLogger(Index.class);
	
	// For WEB-INF/jsp/index-api.jsp
	private static final String VIEW_NAME = "index-api";
		
	@RequestMapping(method={RequestMethod.GET, RequestMethod.HEAD})
	public ModelAndView index() {
		return new KolichControllerClosure<ModelAndView>(
			"[GET,HEAD]:/", logger__) {
			@Override
			public ModelAndView doit() throws Exception {
				return getModelAndView(VIEW_NAME);
			}
		}.execute();
	}
	
}