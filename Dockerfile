FROM gradle:jdk19-alpine AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts /home/gradle/src/
COPY --chown=gradle:gradle src /home/gradle/src/src

RUN gradle shadowJar
FROM openjdk:19-slim

RUN mkdir /app
WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/arkitektonika.jar
ENTRYPOINT ["java", "-Xmx256M", "-jar", "/app/arkitektonika.jar"]