package hamburg.schwartau.datasetup.bootstrap;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

public class DataSetupMain {

    public static void main(String[] args) {
        final Keycloak keycloakClient = createKeycloakClient();
        new RealmSetup(keycloakClient).execute();
        new UserSetup(keycloakClient).execute();
        new ClientMapperSetup(keycloakClient).execute();
        System.out.println("The data has been imported");
    }

    private static Keycloak createKeycloakClient() {
        return KeycloakBuilder.builder()
                .serverUrl("http://localhost:11080/auth")
                .realm("master")
                .username("admin")
                .password("password")
                .clientId("admin-cli")
                .build();
    }
}
