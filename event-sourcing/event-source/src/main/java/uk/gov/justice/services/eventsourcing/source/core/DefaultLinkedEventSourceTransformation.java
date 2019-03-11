package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.linkedevent.ActiveStreamsProcessor;
import uk.gov.justice.services.eventsourcing.linkedevent.LinkedEventsProcessor;
import uk.gov.justice.services.eventsourcing.source.core.exception.LinkedEventException;

import javax.inject.Inject;

/**
 * Implementation of {@link EventSourceTransformation}
 */
public class DefaultLinkedEventSourceTransformation implements LinkedEventSourceTransformation {

    @Inject
    private ActiveStreamsProcessor activeStreamsProcessor;

    @Inject
    private LinkedEventsProcessor linkedEventsProcessor;


    @Override
    public void truncate() throws LinkedEventException {
        linkedEventsProcessor.truncateLinkedEvents();
    }

    @Override
    public void populate() throws LinkedEventException {
        activeStreamsProcessor.populateLinkedEvents();
    }
}
