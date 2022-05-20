#!/usr/bin/env bash

CONTEXT_NAME=framework
EVENT_STORE_VERSION=11.0.0-M15-SNAPSHOT

#fail script on error
set -e


function runEventBufferLiquibase() {
    echo "running EventBufferLiquibase"
    java -jar target/event-buffer-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}viewstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
    echo "Finished EventBufferLiquibase"
}


runEventBufferLiquibase