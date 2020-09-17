FROM gradle:latest AS builder
USER root
RUN mkdir /workspace
ADD . /workspace
RUN cd /workspace && gradle shadowJar --no-daemon

FROM openjdk:8-jre-slim AS java

ARG GITHUB_OAUTH
ARG GITHUB_LOGIN

ENV GITHUB_OAUTH_ENV $GITHUB_OAUTH
ENV GITHUB_LOGIN_ENV $GITHUB_LOGIN


USER root
RUN mkdir /workspace
COPY --from=builder /workspace/build/libs /workspace
RUN java -jar /workspace/datainfrastructure-1.0-SNAPSHOT-all.jar $GITHUB_OAUTH_ENV $GITHUB_LOGIN_ENV

FROM nginx:latest
USER root
COPY --from=java /index.html /index.html
RUN cp /index.html /usr/share/nginx/html/index.html