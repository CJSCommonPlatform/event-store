package uk.gov.justice.services.eventsourcing.repository.jdbc;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.LoggerFactory;

@ApplicationScoped
public class EventRepositoryFactory {

    @Inject
    EventConverter eventConverter;

    public EventRepository eventRepository(
            final EventJdbcRepository eventJdbcRepository,
            final EventStreamJdbcRepository eventStreamJdbcRepository,
            final PublishedEventFinder publishedEventFinder) {

        return new JdbcBasedEventRepository(
                eventConverter,
                eventJdbcRepository,
                eventStreamJdbcRepository,
                publishedEventFinder,
                LoggerFactory.getLogger(EventRepository.class)
        );
    }
}
