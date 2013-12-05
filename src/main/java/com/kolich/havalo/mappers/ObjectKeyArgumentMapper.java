/**
 * Copyright (c) 2013 Mark S. Kolich
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

import com.kolich.common.util.URLEncodingUtils;
import com.kolich.curacao.annotations.mappers.ControllerArgumentTypeMapper;
import com.kolich.curacao.handlers.requests.mappers.ControllerMethodArgumentMapper;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

import static org.apache.commons.lang3.Validate.notEmpty;

@ControllerArgumentTypeMapper(ObjectKeyArgumentMapper.ObjectKey.class)
public final class ObjectKeyArgumentMapper
	extends ControllerMethodArgumentMapper<ObjectKeyArgumentMapper.ObjectKey> {

    // In a path "/api/foo/{key}", this maps to {key}
    private static final String PATHVAR_KEY = "key";

	@Override
	public final ObjectKey resolve(@Nullable final Annotation annotation,
        final CuracaoRequestContext context) {
        return new ObjectKey(context.getPathVars().get(PATHVAR_KEY));
	}

    public static final class ObjectKey {

        private final String decodedKey_;

        public ObjectKey(final String key) {
            notEmpty(key, "Key cannot be null or empty.");
            // URL-decode the incoming key (the name of the object)
            decodedKey_ = URLEncodingUtils.urlDecode(key);
        }

        public final String getDecodedKey() {
            return decodedKey_;
        }

        @Override
        public final String toString() {
            return decodedKey_;
        }

    }
	
}
