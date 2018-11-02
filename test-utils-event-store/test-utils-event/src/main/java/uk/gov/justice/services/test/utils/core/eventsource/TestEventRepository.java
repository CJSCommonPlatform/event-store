package uk.gov.justice.services.test.utils.core.eventsource;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Standalone repository class to access event streams. To be used in integration testing. Moved
 * here from framework by Allan
 */
public class TestEventRepository {

    private static final String POSTGRES_DRIVER_NAME = "org.postgresql.Driver";

    private final EventJdbcRepository eventJdbcRepository;
    private final DataSource dbSource;

    public TestEventRepository(final DataSource dbSource) {
        this.dbSource = dbSource;
        eventJdbcRepository = new EventJdbcRepository(
                new AnsiSQLEventLogInsertionStrategy(),
                new JdbcRepositoryHelper(),
                null,
                "",
                getLogger(EventJdbcRepository.class));

        ReflectionUtils.setField(eventJdbcRepository, "dataSource", dbSource);
    }

    public TestEventRepository(final String url, final String username, final String password, final String driverClassName) {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        this.dbSource = dataSource;

        eventJdbcRepository = new EventJdbcRepository(
                new AnsiSQLEventLogInsertionStrategy(),
                new JdbcRepositoryHelper(),
                null,
                "",
                getLogger(EventJdbcRepository.class));

        ReflectionUtils.setField(eventJdbcRepository, "dataSource", dbSource);
    }

    public TestEventRepository(final String contextName) {
        this(jdbcUrlFrom(contextName), contextName, contextName, POSTGRES_DRIVER_NAME);
    }

    public static TestEventRepository forContext(final String contextName) {
        return new TestEventRepository(contextName);
    }

    public List<Event> eventsOfStreamId(final UUID streamId) {
        try (final Stream<Event> events = eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamId)) {
            return events.collect(toList());
        }
    }

    public List<Event> allEvents() {
        try (final Stream<Event> events = eventJdbcRepository.findAll()) {
            return events.collect(toList());
        }
    }

    public void insert(final Event event) throws InvalidPositionException {
        eventJdbcRepository.insert(event);
    }

    public DataSource getDataSource() {
        return dbSource;
    }

    private static String jdbcUrlFrom(final String contextName) {
        return String.format("jdbc:postgresql://%s/%seventstore", getHost(), contextName);
    }
}
