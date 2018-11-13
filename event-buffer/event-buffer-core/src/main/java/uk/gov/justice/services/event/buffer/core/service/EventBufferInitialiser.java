package uk.gov.justice.services.event.buffer.core.service;

import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.event.buffer.core.repository.subscription.SubscriptionJdbcRepository;

import java.util.UUID;

import javax.inject.Inject;

public class EventBufferInitialiser {

    private static final long INITIAL_VERSION = 0L;

    @Inject
    private SubscriptionJdbcRepository subscriptionJdbcRepository;

    /**
     * Initialises buffer (if not already intialised) and returns the current version of the buffer
     * status
     *
     * @param streamId - id of the stream to be initialised
     * @return - version of the last event that was in order
     */
    public long initialiseBuffer(final UUID streamId, final String source) {
        subscriptionJdbcRepository.updateSource(streamId,source);
        subscriptionJdbcRepository.insertOrDoNothing(new Subscription(streamId, INITIAL_VERSION, source));
        return subscriptionJdbcRepository.findByStreamIdAndSource(streamId, source)
                .orElseThrow(() -> new IllegalStateException("stream status cannot be empty"))
                .getPosition();
    }
}
