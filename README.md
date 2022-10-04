# Keycloak custom protocol mapper example / customize JWT tokens

Per default [Keycloak](https://www.keycloak.org/) writes a lot of things into the [JWT tokens](https://tools.ietf.org/html/rfc7519),
e.g. the preferred username. If that is not enough, a lot of additional built in protocol mappers can be added to customize
the [JWT token](https://tools.ietf.org/html/rfc7519) created by [Keycloak](https://www.keycloak.org/) even further. They can be added in the client
section via the mappers tab (see the [documentation](https://www.keycloak.org/docs/latest/server_admin/index.html#_protocol-mappers)). But sometimes the build
in protocol mappers are not enough. If this is the case, an own protocol mapper can be added to [Keycloak](https://www.keycloak.org/) via an (not yet)
official [service provider API](https://www.baeldung.com/java-spi). This project shows how this can be done.

## Entrypoints into this project

1. [data-setup](data-setup): Project to configure [Keycloak](https://www.keycloak.org/) via its REST API. Configures a realm so that it uses the example
   protocol mapper. Contains a [main method](data-setup/src/main/java/hamburg/schwartau/datasetup/bootstrap/DataSetupMain.java) which can be executed against a
   running [Keycloak](https://www.keycloak.org/) instance. Doesn't need to be executed manually because it's executed automatically by
   the `docker-entrypoint.sh` during startup.
2. [protocol-mapper](protocol-mapper): Contains the protocol mapper code. The resulting jar file will be deployed to [Keycloak](https://www.keycloak.org/). I
   tried to explain things needed in comments in the [protocol-mapper project](protocol-mapper)
3. [Dockerfile](Dockerfile): Adds the jar file containing the [protocol mapper](protocol-mapper/src/main/java/hamburg/schwartau/HelloWorldMapper.java), created
   by the [protocol-mapper project](protocol-mapper), to the keycloak instance.

## Try it out

To try it out do the following things:

### Konfiguration of keycloak

1. If you have already started this project and changed something, execute `docker-compose down -v` so
   that the volumes and so on are destroyed. Otherwise the old keycloak in memory
   database might be reused or you might not see your changed data.
2. Start build and start keycloak using docker: `docker-compose up --build`.
3. After the keycloak has been started, the [main class `DataSetupMain`](data-setup/src/main/java/hamburg/schwartau/datasetup/bootstrap/DataSetupMain.java) in
   our [data-setup](data-setup) module should be started automatically by the `docker-entrypoint.sh` in the Dockerfile and should add some example data to the
   keycloak instance. You should see the message `The data has been imported` in the console if it has been executed successfully.
3. Now you can open the [Keycloak admin console](http://localhost:11080/auth/admin/) and login with username / password: admin / password.
   This initial password for the admin user were configured in our [docker-compose](docker-compose.yml) file.
4. You should see that the master and an example realm, which was added by the [data-setup](data-setup) module automatically, exists currently. For this example
   realm the [hello world mapper](protocol-mapper/src/main/java/hamburg/schwartau/HelloWorldMapper.java) is
   configured: ![Keycloak screenshot](images/keycloak_mapper.png?raw=true "Keycloak screenshot")

Now [Keycloak](https://www.keycloak.org/) is configured. As a next step we want to check the token.

### Checking the access token

To check the token, we need to login. To get the tokens using the direct flow (not recommended for production usage, just for easy demo purposes. See
this [page](https://auth0.com/docs/api-auth/which-oauth-flow-to-use)) execute the following curl command:

    curl -d 'client_id=example-realm-client' -d 'username=jdoe' -d 'password=password' -d 'grant_type=password' 'http://localhost:11080/auth/realms/example-realm/protocol/openid-connect/token'

Note that using the direct flow is only possible because we configured keycloak to allow it in
the [`RealmSetup` class](data-setup/src/main/java/hamburg/schwartau/datasetup/bootstrap/RealmSetup.java).
Response should be like:

    {
      "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJuY0dFQmhya0ZwekllaTdYYzlkRkkydWF3NGllS0plSzQ5YkNvMmtHc2xJIn0.eyJleHAiOjE2NjQ5MDUxODksImlhdCI6MTY2NDkwNDg4OSwianRpIjoiZTEwNzI3NmQtZDk3NS00MjIyLWJkYjUtNDgyY2Y5NzhhOTk4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDoxMTA4MC9hdXRoL3JlYWxtcy9leGFtcGxlLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjRiOTUxNjZjLTQxMzctNGRkNi04YTBkLTM4OTI0MWVhNWMzYiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImV4YW1wbGUtcmVhbG0tY2xpZW50Iiwic2Vzc2lvbl9zdGF0ZSI6IjU2YWQ5MjRmLWQzOGMtNDgwNy1iZTE1LTg2YTExZWFiOGM2YSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1leGFtcGxlLXJlYWxtIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsInNpZCI6IjU2YWQ5MjRmLWQzOGMtNDgwNy1iZTE1LTg2YTExZWFiOGM2YSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkpvaG4gRG9lIiwiZ3JvdXBzIjpbXSwicHJlZmVycmVkX3VzZXJuYW1lIjoiamRvZSIsImdpdmVuX25hbWUiOiJKb2huIiwiZmFtaWx5X25hbWUiOiJEb2UiLCJleGFtcGxlIjp7Im1lc3NhZ2UiOiJoZWxsbyB3b3JsZCJ9fQ.PDU0YijUK9wDPeMwlxqXbw_FS9L6Q-e1rVrm2mZjLiBsrdb7cEw6JieitnMuJodHI3Y2OIkUsMt5j2jsbsegKJSPyw4VEGrBwYnx8GndhV0inhyYryDpLLpCDIpKpijpsi_QCX-jVncoUCPv9MwtAHh8oynCfjEQitu_qVEfYgGRQHBSqyABtpKx44MefBYZQn4qsKDGh6lhJ9Rv5hcaTyIaS8HVgq0zlU9L0PJ5IexhukFD9RR_pE4xECE2yd8kUzXL0XSileuGkpzS-P2JkIivGW5TeiFsDanMLbHGor4sZVgvT4ujiSxiAKCfLN17OT09dWtYtim7fzG0oGZpTw",
      "expires_in": 300,
      "refresh_expires_in": 1800,
      "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJjYmM0MjU2OC1lMjMxLTQxZGEtYmVmYS0yMjg3NTg1NTc2MGQifQ.eyJleHAiOjE2NjQ5MDY2ODksImlhdCI6MTY2NDkwNDg4OSwianRpIjoiMWQyNzVlM2YtYzQzMi00NzZlLWFkNjMtMmIxM2I1YmE0NTVhIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDoxMTA4MC9hdXRoL3JlYWxtcy9leGFtcGxlLXJlYWxtIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDoxMTA4MC9hdXRoL3JlYWxtcy9leGFtcGxlLXJlYWxtIiwic3ViIjoiNGI5NTE2NmMtNDEzNy00ZGQ2LThhMGQtMzg5MjQxZWE1YzNiIiwidHlwIjoiUmVmcmVzaCIsImF6cCI6ImV4YW1wbGUtcmVhbG0tY2xpZW50Iiwic2Vzc2lvbl9zdGF0ZSI6IjU2YWQ5MjRmLWQzOGMtNDgwNy1iZTE1LTg2YTExZWFiOGM2YSIsInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsInNpZCI6IjU2YWQ5MjRmLWQzOGMtNDgwNy1iZTE1LTg2YTExZWFiOGM2YSJ9.3kXlED7h6Wj68yV3pp4dQ1-6N0USu161eiyRV5YaAlY",
      "token_type": "Bearer",
      "not-before-policy": 0,
      "session_state": "56ad924f-d38c-4807-be15-86a11eab8c6a",
      "scope": "email profile"
    }

Then copy the `access_token` value and decode it, e.g. by using [jwt.io](https://jwt.io/). You'll
get something like the following:

      {
         "exp": 1664905189,
         "iat": 1664904889,
         "jti": "e107276d-d975-4222-bdb5-482cf978a998",
         "iss": "http://localhost:11080/auth/realms/example-realm",
         "aud": "account",
         "sub": "4b95166c-4137-4dd6-8a0d-389241ea5c3b",
         "typ": "Bearer",
         "azp": "example-realm-client",
         "session_state": "56ad924f-d38c-4807-be15-86a11eab8c6a",
         "acr": "1",
         "realm_access": {
         "roles": [
            "default-roles-example-realm",
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
         "scope": "email profile",
         "sid": "56ad924f-d38c-4807-be15-86a11eab8c6a",
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
- I got the idea for how to add a custom protocol mapper to [Keycloak](https://www.keycloak.org/) from
  this [jboss mailing list entry](http://lists.jboss.org/pipermail/keycloak-user/2016-February/004891.html)

## Links

- To use keycloak with an angular app, I found this example app to be helpful: https://github.com/manfredsteyer/angular-oauth2-oidc
- Login Page for the users: Login Url: [http://localhost:11080/auth/realms/example-realm/account](http://localhost:11080/auth/realms/example-realm/account)


