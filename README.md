# Keycloak custom protocol mapper example / customize JWT tokens

Per default [Keycloak](https://www.keycloak.org/) writes a lot of things into the [JWT tokens](https://tools.ietf.org/html/rfc7519),
e.g. the preferred username. If that is not enough, a lot of 
additional built in protocol mappers can be added to customize 
the [JWT token](https://tools.ietf.org/html/rfc7519) created by [Keycloak](https://www.keycloak.org/) even further. They can be added in the client
section via the mappers tab (see the [documentation](https://www.keycloak.org/docs/latest/server_admin/index.html#_protocol-mappers)). But sometimes the build in protocol mappers 
are not enough. If this is the case, an own protocol mapper can be
added to [Keycloak](https://www.keycloak.org/) via an (not yet) official [service provider API](https://www.baeldung.com/java-spi). This project 
shows how this can be done.

## Entrypoints into this project
1. [data-setup](data-setup): Project to configure [Keycloak](https://www.keycloak.org/) via its REST API. Configures a realm so that it uses the example protocol mapper. Contains a [main method](data-setup/src/main/java/hamburg/schwartau/datasetup/bootstrap/DataSetupMain.java) which can be executed against a running [Keycloak](https://www.keycloak.org/) instance.
2. [protocol-mapper](protocol-mapper): Contains the protocol mapper code. The resulting jar file will be deployed to [Keycloak](https://www.keycloak.org/). I tried to explain things needed in comments in the [protocol-mapper project](protocol-mapper)
3. [Dockerfile](Dockerfile): Is based upon the official [Keycloak docker image](https://hub.docker.com/r/jboss/keycloak/). Adds the jar file containing the [protocol mapper](protocol-mapper/src/main/java/hamburg/schwartau/HelloWorldMapper.java), created by the [protocol-mapper project](protocol-mapper), to the keycloak instance.                                   

## Try it out

To try it out do the following things:

### Konfiguration of keycloak
1. Build the project `mvn clean install`
2. If you have already started this project, execute `docker-compose down` so
that the volumes and so on are destroyed. Otherwise the old keycloak in memory
database might be reused which will lead to errors if the set up the database
using our scripts, because they expect a fresh keycloak database. 
3. Start build and start keycloak using docker: `docker-compose up --build`. If you
   started it before, execute `docker-compose down`
4. Now you can open the [Keycloak admin console](http://localhost:11080/auth/admin/) and login with username / password: admin / password.
   This initial password for the admin user were configured in our [docker-compose](docker-compose.yml) file.
5. You'll see that only the master realm exists currently
6. Now execute the [main class `DataSetupMain`](data-setup/src/main/java/hamburg/schwartau/datasetup/bootstrap/DataSetupMain.java) in our [data-setup](data-setup) module. 
   This programs sets up an new example realm which uses our Hello world token mapper.
   If it has been executed, you should see the message `The data has been imported` in the console.
   Furthermore if you open the [Keycloak admin console](http://localhost:11080/auth/admin/) you should
   see the example realm. For this realm the [hello world mapper](protocol-mapper/src/main/java/hamburg/schwartau/HelloWorldMapper.java) is configured: ![Keycloak screenshot](images/keycloak_mapper.png?raw=true "Keycloak screenshot")          

Now [Keycloak](https://www.keycloak.org/) is configured. As a next step we want to check the token.   

### Checking the access token

To check the token, we need to login. To get the tokens using the direct flow (not recommended for production usage, just for easy demo purposes. See this [page](https://auth0.com/docs/api-auth/which-oauth-flow-to-use)) execute the following curl command: 

    curl -d 'client_id=example-realm-client' -d 'username=jdoe' -d 'password=password' -d 'grant_type=password' 'http://localhost:11080/auth/realms/example-realm/protocol/openid-connect/token'

Note that using the direct flow is only possible because we configured keycloak to allow it in the [`RealmSetup` class](data-setup/src/main/java/hamburg/schwartau/datasetup/bootstrap/RealmSetup.java).
Response should be like:

    {
      "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJvVXp0bXNyWmF4YUpHY0xKY2l3cV9uM3c1Rm12QVpYV2xMSDFtWGJEeGpNIn0.eyJqdGkiOiJhNTYzOGNhZC04MDQwLTRjMTItYjk1Ny0xZTM3ZTk2MGU1ZTMiLCJleHAiOjE1NDIzMTM1NjcsIm5iZiI6MCwiaWF0IjoxNTQyMzEzMjY3LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjExMDgwL2F1dGgvcmVhbG1zL2V4YW1wbGUtcmVhbG0iLCJhdWQiOiJleGFtcGxlLXJlYWxtLWNsaWVudCIsInN1YiI6ImZjNzI0NGVkLTg4Y2EtNGYwOC05MGI1LWUxODk5NzhhZTQxYyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImV4YW1wbGUtcmVhbG0tY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiMWQzNjlhNjQtZTM5My00NjEzLWI4N2QtOTgwZDA2Y2U0Y2M1IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6W10sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiSm9obiBEb2UiLCJncm91cHMiOltdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJqZG9lIiwiZ2l2ZW5fbmFtZSI6IkpvaG4iLCJmYW1pbHlfbmFtZSI6IkRvZSIsImV4YW1wbGUiOnsibWVzc2FnZSI6ImhlbGxvIHdvcmxkIn19.b270PBBV498Tb3pL1MStP8QIHGXNCzNupVKDAoyStykf4PHewUPMNi_UvmRFP8QUIAIdXfdt3XQ5S4X9ALImmc4Ik92SUT3scsLrZVEtt21Spv6C73HUjJ-vYNaQ6-Rsb0lUpMhrEObYEiDHXCAobwlLcxwTbZbXOJrxBKwflibSfVxkYUD_DDsT2EW4vY1QVfWEa3IcuLNb--fmrbKoEE_Z20_X808jIsNruIijSfADHxDolg0-QPw95_SjUqlQThvWlVVbT12Xe5YsTKbayKDCP__UqQ0DCetOmnEFHkkG6PxPMLOclDwCg68blry4QrYitmmH5IHsKkvs-DJQeA",
      "expires_in": 300,
      "refresh_expires_in": 1800,
      "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJlNzA1ZGUwNC04NGFiLTRhOGMtYTRhMi03NGEwYzAxZGJhNGMifQ.eyJqdGkiOiJhOTBmMzJhYi0yOWM4LTRiNzctYTBiYS1mMDc1ZjE5NmU0ODEiLCJleHAiOjE1NDIzMTUwNjcsIm5iZiI6MCwiaWF0IjoxNTQyMzEzMjY3LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjExMDgwL2F1dGgvcmVhbG1zL2V4YW1wbGUtcmVhbG0iLCJhdWQiOiJleGFtcGxlLXJlYWxtLWNsaWVudCIsInN1YiI6ImZjNzI0NGVkLTg4Y2EtNGYwOC05MGI1LWUxODk5NzhhZTQxYyIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJleGFtcGxlLXJlYWxtLWNsaWVudCIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjFkMzY5YTY0LWUzOTMtNDYxMy1iODdkLTk4MGQwNmNlNGNjNSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIn0.BhZOitnAbxSRHvzXR4KS1eyZRTgnMhcYSCikKbLXw2I",
      "token_type": "bearer",
      "not-before-policy": 0,
      "session_state": "1d369a64-e393-4613-b87d-980d06ce4cc5",
      "scope": "profile email"
    }

Then copy the `access_token` value and decode it, e.g. by using [jwt.io](https://jwt.io/). You'll
get something like the following:

    {
      "jti": "a5638cad-8040-4c12-b957-1e37e960e5e3",
      "exp": 1542313567,
      "nbf": 0,
      "iat": 1542313267,
      "iss": "http://localhost:11080/auth/realms/example-realm",
      "aud": "example-realm-client",
      "sub": "fc7244ed-88ca-4f08-90b5-e189978ae41c",
      "typ": "Bearer",
      "azp": "example-realm-client",
      "auth_time": 0,
      "session_state": "1d369a64-e393-4613-b87d-980d06ce4cc5",
      "acr": "1",
      "allowed-origins": [],
      "realm_access": {
        "roles": [
          "offline_access",
          "uma_authorization"
        ]
      },
      "resource_access": {
        "account": {
          "roles": [
            "manage-account",
            "manage-account-links",
            "view-profile"
          ]
        }
      },
      "scope": "profile email",
      "email_verified": false,
      "name": "John Doe",
      "groups": [],
      "preferred_username": "jdoe",
      "given_name": "John",
      "family_name": "Doe",
      "example": {
        "message": "hello world"
      }
    }

The value auf our own [Hello World Token mapper](protocol-mapper/src/main/java/hamburg/schwartau/HelloWorldMapper.java) got added to the token because
the message 'hello world' appears in the example.message field.

## Acknowledgements
- Examples for [Keycloak](https://www.keycloak.org/): https://github.com/keycloak/keycloak/tree/master/examples 
- Copied the idea to customize [Keycloak](https://www.keycloak.org/) via XSLT from [this project](https://github.com/arielcarrera/keycloak-docker-oracle)
- I got the idea for how to add a custom protocol mapper to [Keycloak](https://www.keycloak.org/) from this [jboss mailing list entry](http://lists.jboss.org/pipermail/keycloak-user/2016-February/004891.html)

## Links
- To use keycloak with an angular app, I found this example app to be helpful: https://github.com/manfredsteyer/angular-oauth2-oidc 
- Login Page for the users: Login Url: [http://localhost:11080/auth/realms/example-realm/account](http://localhost:11080/auth/realms/example-realm/account)


