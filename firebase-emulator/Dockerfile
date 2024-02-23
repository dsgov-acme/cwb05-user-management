FROM node:20-alpine

ENV FIREBASE_TOOLS_VERSION=13.1.0
RUN yarn global add firebase-tools@${FIREBASE_TOOLS_VERSION} && \
    yarn cache clean && \
    firebase -V && \
    mkdir "$HOME/.cache"

RUN apk --no-cache add openjdk17-jre bash curl nginx gettext sed grep
RUN firebase setup:emulators:ui
RUN mkdir -p /firebase

COPY firebase-emulator/docker-entrypoint.sh /usr/bin/
COPY firebase-emulator/functions /firebase/functions
RUN npm install --prefix /firebase/functions

ENTRYPOINT "/usr/bin/docker-entrypoint.sh"
