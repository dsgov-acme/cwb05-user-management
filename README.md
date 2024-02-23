# dsgov-acme User Management API build and run

## Prerequisites

Make sure you have the following installed:

1. Java 11+
2. Docker
3. Setup and configure minikube (using [This setup](https://github.com/dsgov-acme/cwb05-local-environment))

## Run Locally

1. To just spin up the service in `minikube`, run this command: `skaffold run`
2. once the service is running locally, you can view the specs [here](http://api.cwb05.test/um/swagger-ui/index.html) `http://api.cwb05.test/um/swagger-ui/index.html`

## Develop Locally

1. In a standalone terminal, run: `skaffold dev`
2. You should eventually have console output similar to this:
![Skaffold Dev 1](docs/assets/skaffold-dev-log-1.png)
![Skaffold Dev 2](docs/assets/skaffold-dev-log-2.png)
3. As you make code changes, Skaffold will rebuild the container image and deploy it to your local `minikube` cluster.
4. Once the new deployment is live, you can re-generate your Postman collection to test your new API changes!

To exit `skaffold dev`, in the terminal where you executed the command, hit `Ctrl + C`.

**NOTE: This will terminate your existing app deployment in minikube.**

## Querying Postgres locally via IntelliJ

1. Open the database tab in the top right
2. Add new datasource `PostgresSQL`
3. Add host as `db.cwb05.test` and the port as `30202`
4. Add your database as `local-user-management-db`
5. Add your user as `root` and password as `root`
6. Hit apply and save

### Documentation

- [tools and frameworks](./docs/tools.md)

## Configuration Parameters

Here are the key configuration parameters for the application:
### Helm

#### Postgres
- POSTGRES_HOST: `<db-host-instance-name>`
- POSTGRES_DB: `<db-name>`
- POSTGRES_PASSWORD: `<db-password>`
- POSTGRES_PORT: `<db-port>`
- POSTGRES_USER: `<db-user>`

#### Network
- host: `<api-domain-name>`
- applicationPort: `<k8s-application-container-port>`
- servicePort: `<k8s-service-port>`
- contextPath: `<k8s-ingress-context-path>`
- readinessProbe.path: `<k8s-readiness-probe-path>`

#### Environment Variables
- ALLOWED_ORIGINS: `<allowed-origins>`
- CERBOS_URI: `<cerbos-uri>`
- DB_CONNECTION_URL: `<db-connection-url>`
- DB_USERNAME: `<db-username>`
- DB_PASSWORD: `<db-password>`
- GCP_PROJECT_ID: `<gcp-project-id>`
- SELF_SIGN_PUBLIC_KEY: `<secret-manager-path-to-rsa-public-key>`
- SELF_SIGN_PRIVATE_KEY: `<secret-manager-path-to-rsa-private-key>`
- TOKEN_PRIVATE_KEY_SECRET: `<token-private-key-secret-name>`
- TOKEN_PRIVATE_KEY_VERSION: `<token-private-key-secret-version>`
- TOKEN_ISSUER: `<token-issuer-name>`
- OTEL_SAMPLER_PROBABILITY: `<opentelemetry-sampler-probability>`

### Gradle

#### settings.gradle
- rootProject.name = `<project-name>`

#### gradle-wrapper.properties
- distributionBase=`<distribution-base>`
- distributionPath=`<distribution-path>`
- distributionUrl=`<distribution-url>`
- zipStoreBase=`<zip-store-base>`
- zipStorePath=`<zip-store-path>`
- org.gradle.unsafe.watch-fs=`<bool>`

## Contributors
The dsgov-acme User Management API was originally a private project with contributions from:
- [@Mark-The-Dev](https://github.com/Mark-The-Dev)
- [@jazelke](https://github.com/jazelke)
- [@gcastro12](https://github.com/gcastro12)
- [@gmisail](https://github.com/gmisail)
- [@apengu](https://github.com/apengu)
- [@HarisShahiddsgov-acme](https://github.com/HarisShahiddsgov-acme)
- [@dtsong](https://github.com/dtsong)
