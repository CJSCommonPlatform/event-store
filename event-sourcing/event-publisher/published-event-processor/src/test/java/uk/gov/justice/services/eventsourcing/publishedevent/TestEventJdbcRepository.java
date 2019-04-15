package uk.gov.justice.services.eventsourcing.publishedevent;


import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.eventsourcing.repository.jdbc.PostgresSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import javax.sql.DataSource;

public class TestEventJdbcRepository extends EventJdbcRepository {

    protected final DataSource dataSource;

    public TestEventJdbcRepository(final DataSource dataSource) {
        super(
                new PostgresSQLEventLogInsertionStrategy(),
                new JdbcResultSetStreamer(),
                new PreparedStatementWrapperFactory(),
                dataSource,
                getLogger(TestEventJdbcRepository.class)
        );

        this.dataSource = dataSource;
    }
}
