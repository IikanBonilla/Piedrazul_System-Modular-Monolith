FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN chmod +x mvnw && ./mvnw -B -pl backend/piedrazul-app -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/backend/piedrazul-app/target/piedrazul-app-*.jar /app/piedrazul.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/piedrazul.jar"]
