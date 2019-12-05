package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

public interface EventQueueProcessingConfig {
    int getMaxTotalEventsInProcess();
}
