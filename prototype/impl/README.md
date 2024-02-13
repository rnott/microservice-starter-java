
## Standalone Database
Run a Postgres database as a Docker container. The database is started on the standard
port using the admin credentials specified on the command line. Data is removed 
when the container is stopped.

```
docker run --name dev-postgres --rm \
  -e POSTGRES_USER=dev -e POSTGRES_PASSWORD=4y7sV96vA9wv46VR \
  -e PGDATA=/var/lib/postgresql/data/pgdata -v /tmp:/var/lib/postgresql/data \
  -p 5432:5432 -it postgres:14.1-alpine
``

## SpringBoot Service
Environment specific settings should be specified as command line arguments. This is
particularly true for sensitive information such as credentials. *Never commit 
sensitive information to source control.*

In the following examples, the database credentials used match those used to start
the database.

### Executable Java Archive (JAR)
Note that containerized services such as Postgres are available on the loopback network.

```
java -jar target/example-impl-1.0-SNAPSHOT-bin.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/postgres \
  --spring.datasource.username=dev \
  --spring.datasource.password=4y7sV96vA9wv46VR
```

### Docker Container

SpringBoot offers a plugin that builds a highly optimized, layered image as 
described in [Build Plugins](https://spring.io/guides/topicals/spring-boot-docker).
Alternatively, you can elect to use another Maven plugin such as the Spotify Maven 
Plugin. These plugins deploy to the local Docker registry by default. The registry
can be changed for the spring-boot plugin through the use of the ```DOCKER_HOST```
environment variable.

To build the containerized service without changes to the POM file:
```
mvn clean spring-boot:build-image \
  -Dspring-boot.build-image.imageName=org.rnott.example/example-service
```
There is also an example of using profiles to achieve the same result. The
profile ID is 'container'. You can create your own profiles with the 
configuration you require as needed. To build using the profile:
'''
mvn clean package -P container
'''

To run the containerized service once it is built in Docker:
```
docker run -p 8080:8080 -t org.rnott.example/example-service  \
--spring.datasource.url=jdbc:postgresql://172.17.0.2:5432/postgres \
--spring.datasource.username=dev \
--spring.datasource.password=4y7sV96vA9wv46VR
```
Note that:
* the tag name is the image name from the build step
* containerized services such as Postgres are available on the Docker bridge
network. The actual IP must be discovered after the service container is started.
The easiest way to do this is:

```
docker exec -it dev-postgres ifconfig eth0
````


