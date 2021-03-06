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

package com.kolich.havalo.mappers;

import curacao.CuracaoContext;
import curacao.annotations.Mapper;
import curacao.mappers.request.ControllerArgumentMapper;
import com.kolich.havalo.entities.types.KeyPair;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;

import static com.kolich.havalo.filters.HavaloAuthenticationFilter.HAVALO_AUTHENTICATION_ATTRIBUTE;

@Mapper
public final class KeyPairArgumentMapper
	extends ControllerArgumentMapper<KeyPair> {

	@Override
	public final KeyPair resolve(@Nullable final Annotation annotation,
                                 final CuracaoContext context) {
        final HttpServletRequest request = context.request_;
        return (KeyPair)request.getAttribute(HAVALO_AUTHENTICATION_ATTRIBUTE);
	}
	
}
