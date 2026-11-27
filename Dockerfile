# The application in one image: the backend jar that ./gradlew build makes, with the built frontend inside it.
#   ./gradlew :backend:bootJar && docker build -t frakkin-toasters .
#   docker run -p 8080:8080 -e DATABASE_URL=... -e TOKEN_SECRET=... frakkin-toasters
# It packs the jar rather than building it, so the CI deploys the jar its tests ran against: the deploy job uploads
# that jar with this file to the VPS and builds the image there (.github/workflows/ci.yml, deploy/deploy.sh).
# The settings a deployment gives:
#   DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD     Postgres, e.g. jdbc:postgresql://db:5432/frakkin_toasters
#   TOKEN_SECRET                                       hashes the state tokens the clients echo
#   FRAKKIN_LOGIN_GOOGLE_CLIENTID, ..._CLIENTSECRET    the Google login
#   FRAKKIN_LOGIN_DISCORD_CLIENTID, ..._CLIENTSECRET   the Discord login
#   FRAKKIN_ALLOWEDORIGINS                             other origins the game socket accepts, if any
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home frakkin
COPY backend/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
USER frakkin
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
