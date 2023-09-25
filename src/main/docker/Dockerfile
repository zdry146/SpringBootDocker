#FROM frolvlad/alpine-oraclejdk8:slim

#buiild docker file from latest java8 container image

FROM docker.io/library/openjdk:8

VOLUME /tmp

ARG JAR_FILE=./*.jar
COPY ${JAR_FILE} app.jar

#sh -c 'touch /app.jar'

ENV JAVA_OPTS=""

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]