package uk.gov.justice.services.eventsourcing.source.api.service.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.stream.Stream;

/**
 * Source of published events.
 */
public interface PublishedEventSource {

    /**
     * returns a (Java) stream of all events since the provided event number. That is all events
     * who's eventNumber is greater than the provided event number
     *
     * @param eventNumber An event number to search from
     * @return a Java Stream of Events
     */
    Stream<PublishedEvent> findEventsSince(final long eventNumber);

    Stream<PublishedEvent> findEventRange(final MissingEventRange missingEventRange);
}
