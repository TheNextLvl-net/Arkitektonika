FROM openjdk:19-slim

RUN mkdir /app
WORKDIR /app
COPY . /app

CMD ["java", "-Xmx256M", "-jar", "arkitektonika.jar"]