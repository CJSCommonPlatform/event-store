package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.publishedevent.ActiveStreamsProcessor;
import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventsProcessor;
import uk.gov.justice.services.eventsourcing.source.core.exception.LinkedEventException;

import javax.inject.Inject;

/**
 * Implementation of {@link EventSourceTransformation}
 */
public class DefaultPublishedEventSourceTransformation implements LinkedEventSourceTransformation {

    @Inject
    private ActiveStreamsProcessor activeStreamsProcessor;

    @Inject
    private PublishedEventsProcessor publishedEventsProcessor;


    @Override
    public void truncate() throws LinkedEventException {
        publishedEventsProcessor.truncatePublishedEvents();
    }

    @Override
    public void populate() throws LinkedEventException {
        activeStreamsProcessor.populatePublishedEvents();
    }
}
