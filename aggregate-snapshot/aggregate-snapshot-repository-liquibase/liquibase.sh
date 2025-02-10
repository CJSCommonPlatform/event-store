#!/usr/bin/env bash

CONTEXT_NAME=framework
EVENT_STORE_VERSION=17.101.3-SNAPSHOT

#fail script on error
set -e


function runEventLogAggregateSnapshotLiquibase() {
    echo "Running EventLogAggregateSnapshotLiquibase"
    java -jar target/aggregate-snapshot-repository-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}eventstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
    echo "Finished executing EventLogAggregateSnapshotLiquibase liquibase"
}


runEventLogAggregateSnapshotLiquibase