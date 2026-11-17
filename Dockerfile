# The application in one image: the backend jar, with the frontend inside it once it exists.
#   docker build -t frakkin-toasters .
#   docker run -p 8080:8080 -e DATABASE_URL=... -e TOKEN_SECRET=... frakkin-toasters
# The settings a deployment gives:
#   DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD     Postgres, e.g. jdbc:postgresql://db:5432/frakkin_toasters
#   TOKEN_SECRET                                       hashes the state tokens the clients echo
#   FRAKKIN_LOGIN_GOOGLE_CLIENTID, ..._CLIENTSECRET    the Google login
#   FRAKKIN_LOGIN_DISCORD_CLIENTID, ..._CLIENTSECRET   the Discord login
#   FRAKKIN_ALLOWEDORIGINS                             other origins the game socket accepts, if any
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home frakkin
COPY --from=build /workspace/backend/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
USER frakkin
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
