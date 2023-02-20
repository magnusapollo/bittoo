package com.bittoo.login.resource;

import com.bittoo.cart.db.entity.CartEntity;
import com.bittoo.customer.db.entity.CustomerEntity;
import com.bittoo.login.model.LoginCredentials;
import com.bittoo.login.model.SignUp;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClientConfig;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.Tokens;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Path("/v1/login")
@RequestScoped
public class LoginResource {
  @Inject Keycloak keycloak;
  @Inject OidcClients oidcClients;

  @POST
  public Uni<Tokens> login(LoginCredentials credentials) {
    OidcClientConfig cfg = new OidcClientConfig();
    cfg.setId("test");
    cfg.setClientId("bittoo-keycloak");
    cfg.setAuthServerUrl("http://localhost:8081/realms/bittoo-test/");
    cfg.getCredentials().setSecret("NT5osYvaBDDyxzwKsfqJxDZr2KatbhVg");
    OidcClientConfig.Grant g = new OidcClientConfig.Grant();
    g.setType(OidcClientConfig.Grant.Type.PASSWORD);
    Map<String, Map<String, String>> grantOpt = new HashMap<>();
    cfg.setGrant(g);
    cfg.setGrantOptions(grantOpt);
    HashMap<String, String> passGrantOptions = new HashMap<>();
    grantOpt.put("password", passGrantOptions);
    passGrantOptions.put("username", credentials.getEmail());
    passGrantOptions.put("password", credentials.getPassword());
    Uni<OidcClient> oidcClientUni = oidcClients.newClient(cfg);
    Uni<Tokens> tokensUni = oidcClientUni
            .onItem()
            .ifNotNull()
            .transformToUni((Function<OidcClient, Uni<? extends Tokens>>) OidcClient::getTokens);

    return tokensUni;
  }

  @POST
  @Path("signUp")
  public Response signUp(SignUp credentials) {
    CustomerEntity customerEntity = new CustomerEntity();
    customerEntity.setFullName(credentials.getFullName());
    Uni<String> customerId =
        Panache.<CartEntity>withTransaction(customerEntity::persist)
            .map(inserted -> inserted.getId().toString());
    var keyCloakUser = buildKeyCloakUser(credentials);
    return keycloak.realm("bittoo-test").users().create(keyCloakUser);
  }

  private UserRepresentation buildKeyCloakUser(SignUp credentials) {
    var keyCloakUser = new UserRepresentation();
    keyCloakUser.setEmail(credentials.getEmail());
    keyCloakUser.setEnabled(true);
    keyCloakUser.setFirstName(credentials.getFullName());
    keyCloakUser.setUsername(credentials.getEmail());

    var creds = new CredentialRepresentation();
    creds.setType("password");
    creds.setValue(credentials.getPassword());
    creds.setTemporary(false);
    keyCloakUser.setCredentials(List.of(creds));
    return keyCloakUser;
  }
}
