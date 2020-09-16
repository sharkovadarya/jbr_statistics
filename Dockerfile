FROM gradle:latest AS builder
USER root
RUN mkdir /workspace
ADD . /workspace
RUN cd /workspace && gradle build && gradle shadowJar

FROM openjdk:8-jre-slim AS java
USER root
RUN mkdir /workspace
COPY --from=builder /workspace/build/libs /workspace
RUN java -jar /workspace/datainfrastructure-1.0-SNAPSHOT-all.jar

FROM nginx:latest
USER root
COPY --from=java /index.html /index.html
COPY /index.html /usr/share/nginx/html/index.html