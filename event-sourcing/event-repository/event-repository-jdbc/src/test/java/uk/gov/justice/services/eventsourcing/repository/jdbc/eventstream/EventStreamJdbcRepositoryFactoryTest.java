package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventStreamJdbcRepositoryFactoryTest {

    @Mock
    private JdbcRepositoryHelper eventStreamJdbcRepositoryHelper;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Test
    public void shouldProduceEventStreamJdbcRepository() throws Exception {
        final String jndiDatasource = "java:/app/example/DS.eventstore";

        final EventStreamJdbcRepository eventStreamJdbcRepository = eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource);

        assertThat(eventStreamJdbcRepository, is(CoreMatchers.notNullValue()));

        final JdbcRepositoryHelper eventStreamJdbcRepositoryHelperField = getValueOfField(eventStreamJdbcRepository, "eventStreamJdbcRepositoryHelper", JdbcRepositoryHelper.class);
        assertThat(eventStreamJdbcRepositoryHelperField, is(eventStreamJdbcRepositoryHelper));

        final JdbcDataSourceProvider jdbcDataSourceProviderField = getValueOfField(eventStreamJdbcRepository, "jdbcDataSourceProvider", JdbcDataSourceProvider.class);
        assertThat(jdbcDataSourceProviderField, is(jdbcDataSourceProvider));

        final String jndiDatasourceField = getValueOfField(eventStreamJdbcRepository, "jndiDatasource", String.class);
        assertThat(jndiDatasourceField, is(jndiDatasource));

        final Logger loggerField = getValueOfField(eventStreamJdbcRepository, "logger", Logger.class);
        assertThat(loggerField, is(notNullValue()));
    }
}
