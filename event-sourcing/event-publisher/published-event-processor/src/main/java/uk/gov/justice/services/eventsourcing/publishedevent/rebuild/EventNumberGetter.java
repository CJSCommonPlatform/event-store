package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.lang.String.format;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

public class EventNumberGetter {

    public Long eventNumberFrom(final Event event) {

        return event
                .getEventNumber()
                .orElseThrow(() -> new RebuildException(format(
                        "No eventNumber found for event with id '%s'",
                        event.getId())));
    }
}
