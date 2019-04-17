package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class EventStreamJdbcRepositoryFactory {

    @Inject
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Inject
    private UtcClock clock;

    public EventStreamJdbcRepository eventStreamJdbcRepository(final DataSource dataSource) {

        return new EventStreamJdbcRepository(
                jdbcResultSetStreamer,
                preparedStatementWrapperFactory,
                dataSource,
                clock);
    }
}
