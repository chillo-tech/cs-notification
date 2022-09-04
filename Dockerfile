FROM openjdk:11.0.5-jre-stretch
VOLUME /tmp
EXPOSE 27192
ARG APP_NAME=ms-notifications.jar
ARG JAR_FILE=ms-notifications.jar
ADD  ${JAR_FILE} ms-notifications.jar

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=recette", "/ms-notifications.jar"]

