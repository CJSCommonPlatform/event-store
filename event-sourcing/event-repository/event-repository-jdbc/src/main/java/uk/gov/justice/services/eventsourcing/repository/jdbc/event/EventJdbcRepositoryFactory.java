package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class EventJdbcRepositoryFactory {

    @Inject
    private EventInsertionStrategy eventInsertionStrategy;

    @Inject
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    public EventJdbcRepository eventJdbcRepository(final DataSource dataSource) {
        return new EventJdbcRepository(
                eventInsertionStrategy,
                jdbcResultSetStreamer,
                preparedStatementWrapperFactory,
                dataSource,
                getLogger(EventJdbcRepository.class));
    }
}
