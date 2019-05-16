package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.publishedevent.ActiveStreamsRepublisher;
import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventsProcessor;
import uk.gov.justice.services.eventsourcing.source.core.exception.PublishedEventException;

import javax.inject.Inject;

/**
 * Implementation of {@link EventSourceTransformation}
 */
public class DefaultPublishedEventSourceTransformation implements PublishedEventSourceTransformation {

    @Inject
    private ActiveStreamsRepublisher activeStreamsRepublisher;

    @Inject
    private PublishedEventsProcessor publishedEventsProcessor;

    @Override
    public void deleteAllPublishedEvents() throws PublishedEventException {
        publishedEventsProcessor.truncatePublishedEvents();
    }

    @Override
    public void populatePublishedEvents() throws PublishedEventException {
        activeStreamsRepublisher.populatePublishedEvents();
    }
}
