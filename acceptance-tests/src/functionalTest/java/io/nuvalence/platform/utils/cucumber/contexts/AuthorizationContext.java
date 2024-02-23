package io.nuvalence.platform.utils.cucumber.contexts;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import io.nuvalence.auth.token.SelfSignedTokenGenerator;
import io.nuvalence.auth.util.RsaKeyUtility;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Handles auth token management for scenarios.
 */
@SuppressWarnings({"PMD.BeanMembersShouldSerialize"})
public class AuthorizationContext {
    private static final String gcpProjectId = System.getenv("GCP_PROJECT_ID");
    private static final String tokenPrivateKeySecret = System.getenv("TOKEN_PRIVATE_KEY_SECRET");
    private static final String issuer = System.getenv("TOKEN_ISSUER");
    private static final String secretVersion = System.getenv("TOKEN_PRIVATE_KEY_VERSION");

    private final SelfSignedTokenGenerator tokenGenerator;
    private String token;

    /**
     * Configures token generator with data from GCP Secret Manager.
     *
     * @throws IOException if there is an error initializing token generator
     */
    public AuthorizationContext() throws IOException {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName =
                    SecretVersionName.of(gcpProjectId, tokenPrivateKeySecret, "latest");
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            String key = response.getPayload().getData().toStringUtf8();
            tokenGenerator =
                    new SelfSignedTokenGenerator(
                            issuer,
                            Duration.ofMinutes(5),
                            RsaKeyUtility.getPrivateKeyFromString(key));
        }
    }

    public void generateToken(final String subject, final List<String> roles) {
        token = tokenGenerator.generateToken(subject, roles);
    }

    public String getToken() {
        return token;
    }

    @SuppressWarnings({"PMD.NullAssignment"})
    public void clear() {
        token = null;
    }
}
