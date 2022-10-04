package hamburg.schwartau.datasetup.bootstrap;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.function.Consumer;


public class PopulateTestdataCompletedRealmSetup {

    public static final String POPULATE_TESTDATA_COMPLETED_REALM = "populateTestdataCompleted";

    private final Keycloak keycloak;

    private PopulateTestdataCompletedRealmSetup(final Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    private void createMarkerRealm() {
        final RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setId(POPULATE_TESTDATA_COMPLETED_REALM);
        realmRepresentation.setRealm(POPULATE_TESTDATA_COMPLETED_REALM);
        realmRepresentation.setDisplayName(POPULATE_TESTDATA_COMPLETED_REALM);
        realmRepresentation
            .setDisplayNameHtml("Marker realm so that we know that the test data population has been executed successfully");
        keycloak.realms()
            .create(realmRepresentation);
    }

    private boolean doesRealmAlreadyExist() {
        return this.keycloak.realms()
            .findAll()
            .stream()
            .anyMatch(r -> POPULATE_TESTDATA_COMPLETED_REALM.equals(r.getId()));
    }

    public static PopulateTestdataCompletedRealmSetup forTestSystem(final Keycloak keycloak) {
        return new PopulateTestdataCompletedRealmSetup(keycloak);
    }

    /**
     * Executes the given keycloak initialization only once. Does this by creating a marker realm at the end. This marker realm
     * is useful because it with it we know if the complete initlizarion has been executed in case this initialization routine dies.
     */
    public void executeTestdataPopulationOnlyOnce(Consumer<Keycloak> keycloakInitialization) {
        if (doesRealmAlreadyExist()) {
            System.out.println("Keycloak already contains realm data, so no data has been added to the database");
            return;
        }

        keycloakInitialization.accept(this.keycloak);

        createMarkerRealm();
        System.out.println("The data has been imported");


    }

}
