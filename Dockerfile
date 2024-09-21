FROM gradle:jdk21-alpine AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts /home/gradle/src/
COPY --chown=gradle:gradle src /home/gradle/src/src

RUN gradle shadowJar

FROM openjdk:21-slim

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/arkitektonika.jar /app/arkitektonika.jar

CMD ["java", "-Xmx256M", "-jar", "/app/arkitektonika.jar"]