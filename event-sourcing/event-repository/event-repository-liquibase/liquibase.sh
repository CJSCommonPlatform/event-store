#!/usr/bin/env bash

CONTEXT_NAME=framework
FRAMEWORK_VERSION=11.0.0-M12
EVENT_STORE_VERSION=11.0.0-M14-SNAPSHOT

#fail script on error
set -e


function runEventLogLiquibase() {
    echo "Running event log Liquibase"
    java -jar target/event-repository-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}eventstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
    echo "Finished running event log liquibase"
}


#function runEventBufferLiquibase() {
#    echo "Running event buffer liquibase"
#    java -jar target/event-buffer-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}viewstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
#    echo "finished running event buffer liquibase"
#}
#
#
#function runEventLogAggregateSnapshotLiquibase() {
#    echo "Running aggregate snapshot liquibase"
#    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.event-store:aggregate-snapshot-repository-liquibase:${EVENT_STORE_VERSION}:jar
#    java -jar target/aggregate-snapshot-repository-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}eventstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
#    echo "Finished running aggregate snapshot liquibase"
#}
#
#
#function runViewStoreLiquibase {
#    echo "Running view store liquibase"
#    mvn -f ${CONTEXT_NAME}-viewstore/${CONTEXT_NAME}-viewstore-liquibase/pom.xml -Dliquibase.url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}viewstore -Dliquibase.username=${CONTEXT_NAME} -Dliquibase.password=${CONTEXT_NAME} -Dliquibase.logLevel=info resources:resources liquibase:update
#    echo "Finished executing vew store liquibase"
#}
#
#function runSystemLiquibase {
#    echo "Running system liquibase"
#    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.services:framework-system-liquibase:${FRAMEWORK_VERSION}:jar
#    java -jar target/framework-system-liquibase-${FRAMEWORK_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}system --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
#    echo "Finished executing system liquibase"
#}
#
#function runEventTrackingLiquibase {
#    echo "Running event tracking liquibase"
#    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.event-store:event-tracking-liquibase:${EVENT_STORE_VERSION}:jar
#    java -jar target/event-tracking-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}viewstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
#    echo "Finished executing event tracking liquibase"
#}
#

runEventLogLiquibase