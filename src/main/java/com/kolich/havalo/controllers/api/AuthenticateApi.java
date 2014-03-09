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

package com.kolich.havalo.controllers.api;

import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.methods.POST;
import com.kolich.curacao.handlers.requests.matchers.AntPathMatcher;
import com.kolich.havalo.components.RepositoryManagerComponent;
import com.kolich.havalo.controllers.HavaloApiController;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.filters.HavaloAuthenticationFilter;

@Controller
public class AuthenticateApi extends HavaloApiController {

    @Injectable
    public AuthenticateApi(final RepositoryManagerComponent component) {
        super(component.getRepositoryManager());
    }

    @POST(value="/api/authenticate",
            matcher=AntPathMatcher.class,
            filter=HavaloAuthenticationFilter.class)
    public final KeyPair authenticate(final KeyPair userKp) {
        // A bit redundant, but the call to getRepository() here just
        // verifies that the user account exists ~and~ the corresponding
        // repository exists in the system as well.
        return getRepository(userKp.getKey()).getKeyPair();
    }

}
