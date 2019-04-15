package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamJdbcRepositoryFactoryTest {

    @Mock
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Mock
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Test
    public void shouldProduceEventStreamJdbcRepository() throws Exception {

        final DataSource dataSource = mock(DataSource.class);
        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(dataSource);

        assertThat(eventStreamJdbcRepository, is(notNullValue()));

        assertThat(getValueOfField(eventStreamJdbcRepository, "jdbcResultSetStreamer", JdbcResultSetStreamer.class), is(jdbcResultSetStreamer));
        assertThat(getValueOfField(eventStreamJdbcRepository, "preparedStatementWrapperFactory", PreparedStatementWrapperFactory.class), is(preparedStatementWrapperFactory));

        assertThat(getValueOfField(eventStreamJdbcRepository, "dataSource", DataSource.class), is(dataSource));
        assertThat(getValueOfField(eventStreamJdbcRepository, "clock", UtcClock.class), is(clock));
    }
}
