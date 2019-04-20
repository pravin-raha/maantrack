FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/scala-2.12/*.jar app.jar
EXPOSE 8080

CMD  java -jar app.jar