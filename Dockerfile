FROM openjdk:17-jdk-slim
VOLUME /tmp
EXPOSE 9092
ARG APP_NAME=ms-notifications.jar
ARG JAR_FILE=target/ms-notifications.jar

ADD  ${JAR_FILE} ms-notifications.jar

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=recette", "/ms-notifications.jar"]

