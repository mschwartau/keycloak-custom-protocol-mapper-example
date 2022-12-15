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
   configured (in [clients=>example-realm-client=>Client scopes=>dedicated](http://localhost:11080/auth/admin/master/console/#/example-realm/clients/example-realm-client/clientScopes/dedicated)): ![Keycloak screenshot](images/keycloak_mapper.png?raw=true "Keycloak screenshot")

Now [Keycloak](https://www.keycloak.org/) is configured. As a next step we want to check the token.

### Checking the access token

To check the token, we need to login. To get the tokens using the direct flow (not recommended for production usage, just for easy demo purposes. See
this [page](https://auth0.com/docs/api-auth/which-oauth-flow-to-use)) execute the following curl command:

    curl -d 'client_id=example-realm-client' -d 'username=jdoe' -d 'password=password' -d 'grant_type=password' 'http://localhost:11080/auth/realms/example-realm/protocol/openid-connect/token'

Note that using the direct flow is only possible because we configured keycloak to allow it in
the [`RealmSetup` class](data-setup/src/main/java/hamburg/schwartau/datasetup/bootstrap/RealmSetup.java).
Response should be like:

    {
      "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJYbl9PXzN6VHJpSjBzOE5RUzlpMVpBcF9pZVN2YXRwOHRIWmtpTGNwM1RrIn0.eyJleHAiOjE2NzExMzMzMjMsImlhdCI6MTY3MTEzMzAyMywianRpIjoiYTcwYjA4NjQtNmI3Mi00MjljLTliMDEtZWIzNzBhMTE5YTgzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDoxMTA4MC9hdXRoL3JlYWxtcy9leGFtcGxlLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjkxMmNkZmJhLWNlNGQtNDgzMS04NjA3LWQzM2VmOTkzOTdmYyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImV4YW1wbGUtcmVhbG0tY2xpZW50Iiwic2Vzc2lvbl9zdGF0ZSI6ImRlMzljN2M2LTM0ZWMtNGM4MC1iZTM2LWIyODE4YTkxMjMyYyIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1leGFtcGxlLXJlYWxtIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsInNpZCI6ImRlMzljN2M2LTM0ZWMtNGM4MC1iZTM2LWIyODE4YTkxMjMyYyIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkpvaG4gRG9lIiwiZ3JvdXBzIjpbXSwicHJlZmVycmVkX3VzZXJuYW1lIjoiamRvZSIsImdpdmVuX25hbWUiOiJKb2huIiwiZmFtaWx5X25hbWUiOiJEb2UiLCJleGFtcGxlIjp7Im1lc3NhZ2UiOiJoZWxsbyB3b3JsZCJ9fQ.wZI33cy6X2yxnsz1HeU3snrPi8xg1Pq8TiNIxPfP-RLtPQm5-3of9kTFXNvtZkA2Om3rzlI_NfyYy8eq4VArujVvvkKx5oxGZ0Q9Tv6LU0ufS4YfW0t0oAbEdNmONBXUszcl_HKX_5Pnvbs7DwR04ErAmzguECnky9hdYy0nJREnfrTwr6Ss270H8HaQ-DJ1T4x-iFzuwRkQZTg_PUfRxts0tjsIRehFPxadLujj4ZpsguvfXqCD11Gb4a2xXSm6S2iDP8sa_zwaWCbRDraBUCcEy192hADDNVDBQPYgUe-0Sj7z_mPNviEiMagAmBFCj8W-czkEWwnX_WodeVThWA",
      "expires_in": 300,
      "refresh_expires_in": 1800,
      "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIzODE5NTdiZi1jMzI0LTQ3M2UtOTA4MS1lN2MxODVmMzllYjUifQ.eyJleHAiOjE2NzExMzQ4MjMsImlhdCI6MTY3MTEzMzAyMywianRpIjoiYWM4Njk0NzktYzY2Yi00YWIwLWIzYzQtZDc1ZjU0NWZmOTk3IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDoxMTA4MC9hdXRoL3JlYWxtcy9leGFtcGxlLXJlYWxtIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDoxMTA4MC9hdXRoL3JlYWxtcy9leGFtcGxlLXJlYWxtIiwic3ViIjoiOTEyY2RmYmEtY2U0ZC00ODMxLTg2MDctZDMzZWY5OTM5N2ZjIiwidHlwIjoiUmVmcmVzaCIsImF6cCI6ImV4YW1wbGUtcmVhbG0tY2xpZW50Iiwic2Vzc2lvbl9zdGF0ZSI6ImRlMzljN2M2LTM0ZWMtNGM4MC1iZTM2LWIyODE4YTkxMjMyYyIsInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsInNpZCI6ImRlMzljN2M2LTM0ZWMtNGM4MC1iZTM2LWIyODE4YTkxMjMyYyJ9.AKWuXIuq__KzZC32GrGlhbDe_gZkyQsqKRSIDBKSgJQ",
      "token_type": "Bearer",
      "not-before-policy": 0,
      "session_state": "de39c7c6-34ec-4c80-be36-b2818a91232c",
      "scope": "email profile"
    }

Then copy the `access_token` value and decode it, e.g. by using [jwt.io](https://jwt.io/). You'll
get something like the following:

      {
         "exp": 1671133323,
         "iat": 1671133023,
         "jti": "a70b0864-6b72-429c-9b01-eb370a119a83",
         "iss": "http://localhost:11080/auth/realms/example-realm",
         "aud": "account",
         "sub": "912cdfba-ce4d-4831-8607-d33ef99397fc",
         "typ": "Bearer",
         "azp": "example-realm-client",
         "session_state": "de39c7c6-34ec-4c80-be36-b2818a91232c",
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
         "sid": "de39c7c6-34ec-4c80-be36-b2818a91232c",
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


