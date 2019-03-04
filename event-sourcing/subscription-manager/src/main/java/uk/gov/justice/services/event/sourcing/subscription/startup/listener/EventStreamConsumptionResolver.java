package uk.gov.justice.services.event.sourcing.subscription.startup.listener;

/**
 * Interface for listening to Event Stream Consumers.
 *
 * An instance of the an EventStreamConsumerListener is passed to the consumer.  The consumer will
 * call the finishedConsuming method once complete.
 */
public interface EventStreamConsumptionResolver {

    /**
     * Called by a consumer when finish is expected.
     *
     * @param finishedProcessingMessage - the message containing the Queue that has been consumed.
     *
     * @return true if all events are consumed, false if there are still events remaining in the queue.
     */
    boolean isEventConsumptionComplete(final FinishedProcessingMessage finishedProcessingMessage);
}
