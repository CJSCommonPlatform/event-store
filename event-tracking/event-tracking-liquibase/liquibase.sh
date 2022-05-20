#!/usr/bin/env bash

CONTEXT_NAME=framework
EVENT_STORE_VERSION=11.0.0-M15-SNAPSHOT

#fail script on error
set -e


function runEventTrackingLiquibase() {
    echo "Running event repository Liquibase"
    java -jar target/event-tracking-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}viewstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
    echo "Finished running event repository liquibase"
}


runEventTrackingLiquibase