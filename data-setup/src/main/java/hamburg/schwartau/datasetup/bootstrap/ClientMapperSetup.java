package hamburg.schwartau.datasetup.bootstrap;

import hamburg.schwartau.HelloWorldMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.protocol.oidc.mappers.GroupMembershipMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import java.util.HashMap;
import java.util.Map;

public class ClientMapperSetup {

    public static final String PROTOCOL = "openid-connect";
    private final Keycloak keycloak;

    public ClientMapperSetup(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void execute() {
        final ClientResource client = this.keycloak.realm(RealmSetup.REALM).clients().get(RealmSetup.CLIENT);
        client.getProtocolMappers().createMapper(createGroupMapper());
        client.getProtocolMappers().createMapper(createHelloWordMapper());
    }

    private ProtocolMapperRepresentation createGroupMapper() {
        ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
        protocolMapperRepresentation.setProtocolMapper(GroupMembershipMapper.PROVIDER_ID);
        protocolMapperRepresentation.setProtocol(PROTOCOL);
        protocolMapperRepresentation.setName("Group mapper");
        Map<String, String> config = new HashMap<>();
        putAccessTokenClaim(config);
        // the name of the property we got from the class GroupMembershipMapper
        config.put("full.path", "true");
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, "groups");
        protocolMapperRepresentation.setConfig(config);
        return protocolMapperRepresentation;
    }

    private ProtocolMapperRepresentation createHelloWordMapper() {
        ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
        protocolMapperRepresentation.setProtocolMapper(HelloWorldMapper.PROVIDER_ID);
        protocolMapperRepresentation.setProtocol(PROTOCOL);
        protocolMapperRepresentation.setName("Hello world mapper");
        Map<String, String> config = new HashMap<>();
        putAccessTokenClaim(config);
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, "example.message");
        protocolMapperRepresentation.setConfig(config);
        return protocolMapperRepresentation;
    }

    static void putAccessTokenClaim(Map<String, String> config) {
        config.put("access.token.claim", "true");
    }
}
