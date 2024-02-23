package io.nuvalence.platform.utils.cucumber.contexts;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.util.Optional;

/**
 * Shared context/data to be retained throughout a test scenario.
 * This is injected by cucumber-picocontainer in any step definitions
 * class which takes this as a constructor argument.
 */
@Getter
@Setter
public class ScenarioContext {
    private static final String baseUri =
            Optional.ofNullable(System.getenv("SERVICE_URI")).orElse("http://api.dsgov.test/um");

    private InputStream loadedResource;

    private final AuthorizationContext authorizationContext;

    public ScenarioContext(AuthorizationContext authorizationContext) {
        this.authorizationContext = authorizationContext;
    }

    /**
     * Adds authorization header to request.
     * @param request http request
     */
    public void applyAuthorization(HttpRequest.Builder request) {
        if (authorizationContext.getToken() != null) {
            request.header("authorization", "Bearer " + authorizationContext.getToken());
        }
    }

    public String getBaseUri() {
        return baseUri;
    }
}
