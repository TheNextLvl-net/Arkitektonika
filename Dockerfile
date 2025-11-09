FROM gradle:jdk25-alpine AS build

WORKDIR /gradle

COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts /gradle/
COPY --chown=gradle:gradle src /gradle/src

RUN gradle shadowJar

FROM eclipse-temurin:25

WORKDIR /app

COPY --from=build /gradle/build/libs/arkitektonika.jar /app/arkitektonika.jar

CMD ["java", "-Xmx256M", "-jar", "/app/arkitektonika.jar"]