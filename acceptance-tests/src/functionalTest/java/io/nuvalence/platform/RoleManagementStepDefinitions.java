package io.nuvalence.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.nuvalence.platform.utils.cucumber.contexts.AuthorizationContext;
import io.nuvalence.platform.utils.cucumber.contexts.ScenarioContext;
import io.nuvalence.user.management.api.client.generated.models.ApplicationRolesDTO;
import io.nuvalence.user.management.api.client.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.client.generated.models.RoleDTO;
import io.nuvalence.user.management.api.client.generated.models.UserDTO;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "PMD.AvoidDuplicateLiterals"})
public class RoleManagementStepDefinitions {

    private static final HttpClient client = HttpClient.newHttpClient();
    private final ScenarioContext scenarioContext;
    private final AuthorizationContext authorizationContext;

    private HttpResponse<String> response;

    private ApplicationRolesDTO applicationManifest;

    private RoleDTO role;
    private RoleDTO responseRole;
    private UserDTO user;

    private List<String> responseRoles;

    private List<PermissionDTO> getPermissionsFromDataTable(DataTable table) {
        return table.asMaps().stream()
                .map(
                        map ->
                                new PermissionDTO()
                                        .name(map.get("name"))
                                        .applicationRole(map.get("applicationRole"))
                                        .group(map.get("group"))
                                        .description(map.get("description")))
                .toList();
    }

    @And("an application role manifest named {string} with roles")
    public void createApplicationManifest(String name, DataTable roles) {
        final List<PermissionDTO> permissions = getPermissionsFromDataTable(roles);

        this.applicationManifest = new ApplicationRolesDTO().name(name).roles(permissions);
    }

    @When("the application role manifest is PUT to the API")
    public void putApplicationManifest()
            throws URISyntaxException, IOException, InterruptedException {
        final String body = new ObjectMapper().writeValueAsString(applicationManifest);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(new URI(scenarioContext.getBaseUri() + "/api/v1/application/roles"))
                        .header("content-type", "application/json")
                        .header("authorization", "Bearer " + authorizationContext.getToken())
                        .PUT(HttpRequest.BodyPublishers.ofString(body))
                        .build();

        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Then("it should return {int}")
    public void itShouldReturn(int expectedStatus) {
        assertNotNull(this.response);
        assertEquals(expectedStatus, this.response.statusCode());
    }

    @When("a GET request is made for application roles")
    public void getApplicationRoles() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(new URI(scenarioContext.getBaseUri() + "/api/v1/application/roles"))
                        .header("authorization", "Bearer " + authorizationContext.getToken())
                        .GET()
                        .build();

        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @And("the response should contain application roles")
    public void containsApplicationRoles(DataTable roles) throws JsonProcessingException {
        final Set<String> expectedRoles =
                roles.asMaps().stream()
                        .map(map -> map.get("applicationRole"))
                        .collect(Collectors.toUnmodifiableSet());

        final PermissionDTO[] permissions =
                new ObjectMapper().readValue(this.response.body(), PermissionDTO[].class);

        final Set<String> foundRoles =
                Arrays.stream(permissions)
                        .map(PermissionDTO::getApplicationRole)
                        .collect(Collectors.toUnmodifiableSet());

        for (String expectedRole : expectedRoles) {
            assertTrue(foundRoles.contains(expectedRole));
        }
    }
}
