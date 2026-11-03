# OAuth Social Login to OpenID Connect

Mit dieser Spring Boot Application kann ein vorhandener OAuth Social Login von [Church Tools](https://church.tools) zu OpenID Connect (OIDC) erweitert werden,
sodass sich eine externe App per OIDC verbinden kann.

# Konfiguration

* In diesem Beispiel ist der Service unter https://openid-customapp.yourchurch.de deployed
* Die externe Anwendung ist: https://custom-app.yourchurch.de

| Umgebungsvariable                    | Wert                                                                   | Erklärung                                        |
|--------------------------------------|------------------------------------------------------------------------|--------------------------------------------------|
| OAUTH_SOCIAL_LOGIN_CLIENT_ID         | clientId von Church Tools                                              |                                                  |
| OAUTH_SOCIAL_LOGIN_SECRET            | zufälliger String                                                      |                                                  |
| OAUTH_SOCIAL_LOGIN_REDIRECTURI       | https://openid-customapp.yourchurch.de/login/oauth2/code/custom-client | /login/oauth2/code/custom-client muss so bleiben |
| OAUTH_SOCIAL_LOGIN_AUTHORIZATION_URI | https://yourchurch.church.tools/oauth/authorize                        |                                                  |
| OAUTH_SOCIAL_LOGIN_TOKEN_URI         | https://yourchurch.church.tools/oauth/access_token                     |                                                  |
| OAUTH_SOCIAL_LOGIN_USER_INFO_URI     | https://yourchurch.church.tools/oauth/userinfo                         |                                                  |
| OPENID_SUB                           | userName                                                               | Identifier des Sub: userName, email, id (Standard: userName).
| OPENID_ISSUER                        | https://openid-customapp.yourchurch.de                                 |                                                  |
| OPENID_CLIENT_ID                     | selbst vergebene ClientId                                              |                                                  |
| OPENID_CLIENT_SECRET                 | selbst vergebenes Secret                                               | Klartext wird beim Start gehasht (bcrypt, ab 73 Bytes PBKDF2); alternativ bereits gehasht mit Präfix angeben, z. B. `{bcrypt}$2a$10$...`. Ein Klartext-Secret darf daher nicht mit `{...}` beginnen. |
| OPENID_REDIRECT_URI                  | https://custom-app.yourchurch.de/oauth2/callback                       |                                                  |
| OPENID_SIGNING_KEY                   | RSA-Schlüssel (PKCS#8) als einzeiliges Base64 oder PEM                 | Empfohlen. Ohne diese Variable wird bei jedem Start ein neuer Schlüssel erzeugt und alle ausgestellten Tokens werden ungültig. Erzeugen siehe unten. |

## Signaturschlüssel erzeugen

Einmalig erzeugen und den Wert als Umgebungsvariable `OPENID_SIGNING_KEY` hinterlegen, z. B. in den
Stack-Umgebungsvariablen von Portainer oder in einer `.env`-Datei neben der `docker-compose.yml`.
Ein Volume ist dafür nicht nötig.

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -outform DER | base64 -w0
```

Der Schlüssel ist geheim und gehört nicht ins Repository. Autorisierungen und Refresh-Tokens liegen weiterhin im Speicher.
Nach einem Neustart meldet der Client den Benutzer daher neu an. Solange die Church-Tools-Sitzung noch aktiv ist,
läuft das ohne Eingabe über Redirects.

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
      - OAUTH_SOCIAL_LOGIN_USER_INFO_URI=https://yourchurch.church.tools/oauth/userinfo
      - OPENID_SUB=userName
      - OPENID_ISSUER=https://openid-customapp.yourchurch.de
      - OPENID_CLIENT_ID=custom-app-client
      - OPENID_CLIENT_SECRET=randomTopSecretString
      - OPENID_REDIRECT_URI=https://custom-app.yourchurch.de/oauth2/callback
      - OPENID_SIGNING_KEY=${OPENID_SIGNING_KEY}
    ports:
      - "8080:8080"
    restart: unless-stopped
```

# JWT Data

Folgende Daten sind im JWT Token verfügbar

## Scope openid

* sub

## Scope Profile
* given_name
* family_name
* name
* preferred_username
* picture

## Scope email
* email
* email_verified (immer `true`, da die E-Mail-Adresse von Church Tools verwaltet wird)


# Build Docker image

```bash
mvn spring-boot:build-image
```

# Publish Image in Dockerhub

```bash
docker tag docker.io/canchanchara/sociallogin-to-openidconnect:latest zendem/sociallogin-to-openidconnect:latest
docker push zendem/sociallogin-to-openidconnect:latest

docker tag docker.io/canchanchara/sociallogin-to-openidconnect:latest zendem/sociallogin-to-openidconnect:<CURRENT_VERSION>
docker push zendem/sociallogin-to-openidconnect:<CURRENT_VERSION>
```