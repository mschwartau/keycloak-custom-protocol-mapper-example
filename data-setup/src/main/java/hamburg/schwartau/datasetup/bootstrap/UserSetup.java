package hamburg.schwartau.datasetup.bootstrap;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.util.Arrays;

public class UserSetup {

    private static String PASSWORD = "password";

    private final UsersResource users;

    public UserSetup(Keycloak keycloak) {
        this.users = keycloak.realm(RealmSetup.REALM).users();
    }

    public void execute() {
        createUser("mmustermann", "Max", "Mustermann");
        createUser("jdoe", "John", "Doe");
    }

    public String createUser(String name, String firstName, String lastName) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(name);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setCredentials(Arrays.asList(createPassword(PASSWORD)));
        Response response = users.create(user);
        return getCreatedId(response);
    }


    private CredentialRepresentation createPassword(final String password) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        return passwordCred;
    }

    private String getCreatedId(Response response) {
        return response.getLocation().toString().replaceAll(".*/", "");
    }
}
