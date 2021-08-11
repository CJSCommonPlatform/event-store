package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

public class EventValidator {

    @Inject
    private PublishedEventSource publishedEventSource;

    @Inject
    private SingleEventValidator singleEventValidator;

    public List<ValidationError> findErrors() {
        try (final Stream<PublishedEvent> publishedSince = publishedEventSource.findEventsSince(0L)) {
            return publishedSince.map(singleEventValidator::validate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());
        }
    }
}
