package hamburg.schwartau.datasetup.bootstrap;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(description = "Does an initial testdata setup for an keycloak database, if the databse already contains data, "
    + "nothing is done",
    name = "initialSetup", mixinStandardHelpOptions = true, version = "1.0")
public class DataSetupMain implements Callable<Integer> {

    @CommandLine.Option(names = { "--restApiBaseUrl" }, description = "BaseUrl for the rest api which is used to set up the data.",
        defaultValue = "http://localhost:11080")
    private String restApiBaseUrl;

    @CommandLine.Option(names = { "--user", "-U" }, description = "Admin user name", interactive = true, defaultValue = "admin")
    private String username;

    @CommandLine.Option(names = { "--password", "-P" }, description = "Admin password", interactive = true, hidden = true, defaultValue =
        "password")
    private String password;

    @Override
    public Integer call() {
        System.out.println("Start to setup keycloak with rest api base url " + restApiBaseUrl);
        PopulateTestdataCompletedRealmSetup.forTestSystem(createKeycloakClient())
            .executeTestdataPopulationOnlyOnce(keycloakClient -> {
                new RealmSetup(keycloakClient).execute();
                new UserSetup(keycloakClient).execute();
                new ClientMapperSetup(keycloakClient).execute();
            });
        return 0;
    }

    public static void main(final String[] args) {
        CommandLine.call(new DataSetupMain(), args);
    }

    private Keycloak createKeycloakClient() {
        return KeycloakBuilder.builder()
            .serverUrl(restApiBaseUrl + "/auth")
            .realm("master")
            .username(username)
            .password(password)
            .clientId("admin-cli")
            .build();
    }

}
