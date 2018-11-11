package hamburg.schwartau.datasetup.bootstrap;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.Arrays;
import java.util.List;


public class RealmSetup {

    static final String REALM = "example-realm";
    static final String CLIENT = "example-realm-client";

    private final Keycloak keycloak;

    public RealmSetup(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void execute() {
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setDisplayName(REALM);
        realmRepresentation.setId(REALM);
        realmRepresentation.setClients(createClients());
        realmRepresentation.setLoginWithEmailAllowed(true);
        realmRepresentation.setEnabled(true);
        realmRepresentation.setRealm(REALM);
        this.keycloak.realms().create(realmRepresentation);
    }

    private List<ClientRepresentation> createClients() {
        ClientRepresentation client = new ClientRepresentation();
        client.setEnabled(true);
        // normally you wouldn't do this, but we use the direct grant to be able
        // to fetch the token for demo purposes per curl ;-)
        client.setDirectAccessGrantsEnabled(true);
        client.setId(CLIENT);
        client.setName(CLIENT);
        client.setPublicClient(Boolean.TRUE);
        return Arrays.asList(client);
    }

}
