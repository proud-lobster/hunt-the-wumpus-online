FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

COPY src ./src
RUN mvn -q -e -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
VOLUME /tmp
ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS

COPY --from=build /app/target/hunt-the-wumpus-online*.jar app.jar

EXPOSE 8443
EXPOSE 5005

ENTRYPOINT ["java"]
CMD ["-jar", "/app/app.jar"]
