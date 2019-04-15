package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventJdbcRepositoryFactoryTest {

    @Mock
    private EventInsertionStrategy eventInsertionStrategy;

    @Mock
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Mock
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @InjectMocks
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Test
    public void shouldProduceEventJdbcRepository() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(dataSource);

        assertThat(eventJdbcRepository, is(notNullValue()));

        assertThat(getValueOfField(eventJdbcRepository, "eventInsertionStrategy", EventInsertionStrategy.class), is(eventInsertionStrategy));
        assertThat(getValueOfField(eventJdbcRepository, "jdbcResultSetStreamer", JdbcResultSetStreamer.class), is(jdbcResultSetStreamer));
        assertThat(getValueOfField(eventJdbcRepository, "preparedStatementWrapperFactory", PreparedStatementWrapperFactory.class), is(preparedStatementWrapperFactory));
        assertThat(getValueOfField(eventJdbcRepository, "dataSource", DataSource.class), is(dataSource));
        assertThat(getValueOfField(eventJdbcRepository, "logger", Logger.class), is(notNullValue()));
    }
}
