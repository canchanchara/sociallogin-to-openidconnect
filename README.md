# OAuth Social Login to OpenID Connect

Mit dieser Spring Boot Application kann ein vorhandener OAuth Social Login zu OpenID Connect (OIDC) erweitert werden,
sodass sich eine externe App per OIDC verbinden kann.

# Konfiguration

* In diesem Beispiel ist der Service unter https://openid-customapp.yourchurch.de deployed
* Die externe Anwendung ist: https://custom-app.yourchurch.de/

| Umgebungsvariable                    | Wert                                                                   | Erklärung                                                                   |
|--------------------------------------|------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| OAUTH_SOCIAL_LOGIN_CLIENT_ID         | clientId von Church Tools                                              |                                                                             |
| OAUTH_SOCIAL_LOGIN_SECRET            | zufälliger String                                                      |                                                                             |
| OAUTH_SOCIAL_LOGIN_REDIRECTURI       | https://openid-customapp.yourchurch.de/login/oauth2/code/custom-client | /login/oauth2/code/custom-client muss so bleiben                            |
| OAUTH_SOCIAL_LOGIN_AUTHORIZATION_URI | https://yourchurch.church.tools/oauth/authorize                        |                                                                             |
| OAUTH_SOCIAL_LOGIN_TOKEN_URI         | https://yourchurch.church.tools/oauth/access_token                     |                                                                             |
| OAUTH_SOCIAL_LOGIN_USER_INFO_URI     | https://yourchurch.church.tools/oauth/userinfo                         |                                                                             |
| DEFAULT_USERNAME                     | userName                                                               | Der Standard Username, der später in sub landet. Alternativ "email" angeben |
| OPENID_ISSUER                        | https://openid-customapp.yourchurch.de                                 |                                                                             |
| OPENID_CLIENT_ID                     | selbst vergebene ClientId                                              |                                                                             |
| OPENID_CLIENT_SECRET                 | selbst vergebenes Secret                                               |                                                                             |
| OPENID_REDIRECT_URI                  | https://custom-app.yourchurch.de/oauth2/callback                       |                                                                             |

# Run with docker-compose

```yaml
services:
  sociallogintoopenid:
    image: zendem/sociallogin-to-openidconnect:latest
    environment:
      - OAUTH_SOCIAL_LOGIN_CLIENT_ID=clientId von Church Tools
      - OAUTH_SOCIAL_LOGIN_SECRET=zufälliger String
      - OAUTH_SOCIAL_LOGIN_REDIRECTURI=https://openid-customapp.yourchurch.de/login/oauth2/code/custom-client
      - OAUTH_SOCIAL_LOGIN_AUTHORIZATION_URI=https://yourchurch.church.tools/oauth/authorize
      - OAUTH_SOCIAL_LOGIN_TOKEN_URI=https://yourchurch.church.tools/oauth/access_token
      - OAUTH_SOCIAL_LOGIN_USER_INFO_URI=https:/yourchurch.church.tools/oauth/userinfo
      - DEFAULT_USERNAME=userName
      - OPENID_ISSUER=https://openid-customapp.yourchurch.de
      - OPENID_CLIENT_ID=custom-app-client
      - OPENID_CLIENT_SECRET=randomTopSecretString
      - OPENID_REDIRECT_URI=https://custom-app.yourchurch.de/oauth2/callback
    ports:
      - "8080:8080"
    restart: unless-stopped
```

# Build Docker image

```bash
mvn spring-boot:build-image
```

# Publish Image in Dockerhub

```bash
docker tag docker.io/canchanchara/sociallogin-to-openidconnect:latest zendem/sociallogin-to-openidconnect:latest
docker push zendem/sociallogin-to-openidconnect:latest
```