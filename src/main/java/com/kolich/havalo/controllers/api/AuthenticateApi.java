package com.kolich.havalo.controllers.api;

import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.methods.POST;
import com.kolich.havalo.components.RepositoryManagerComponent;
import com.kolich.havalo.controllers.HavaloApiController;
import com.kolich.havalo.entities.types.KeyPair;
import com.kolich.havalo.filters.HavaloAuthenticationFilter;
import com.kolich.havalo.io.managers.RepositoryManager;

@Controller
public class AuthenticateApi extends HavaloApiController {

    @Injectable
    public AuthenticateApi(final RepositoryManagerComponent component) {
        super(component.getRepositoryManager());
    }

    @POST(value="/api/authenticate", filter=HavaloAuthenticationFilter.class)
    public final KeyPair authenticate(final KeyPair userKp) {
        // A bit redundant, but the call to getRepository() here just
        // verifies that the user account exists ~and~ the corresponding
        // repository exists in the system as well.
        return getRepository(userKp.getKey()).getKeyPair();
    }

}
