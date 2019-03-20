package uk.gov.justice.services.eventsourcing.linkedevent;


import static org.slf4j.LoggerFactory.getLogger;

import uk.gov.justice.services.eventsourcing.repository.jdbc.PostgresSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import javax.sql.DataSource;

public class TestEventJdbcRepository extends EventJdbcRepository {

    protected final DataSource dbsource;

    public TestEventJdbcRepository(final DataSource datasource) {
        super(
                new PostgresSQLEventLogInsertionStrategy(),
                new JdbcRepositoryHelper(),
                jndiName -> datasource,
                "java:/app/EventDeQueuerExecutorIT/DS.frameworkeventstore",
                getLogger(TestEventJdbcRepository.class)
        );

        this.dbsource = datasource;
    }

    protected DataSource getDataSource() {
        return dbsource;
    }
}
