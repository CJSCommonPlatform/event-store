package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;

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
    private JdbcRepositoryHelper jdbcRepositoryHelper;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @InjectMocks
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Test
    public void shouldProduceEventJdbcRepository() throws Exception {
        final String jndiDatasource = "java:/app/example/DS.frameworkeventstore";

        final EventJdbcRepository eventJdbcRepository = eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource);

        assertThat(eventJdbcRepository, is(notNullValue()));

        final EventInsertionStrategy eventInsertionStrategyField = getValueOfField(eventJdbcRepository, "eventInsertionStrategy", EventInsertionStrategy.class);
        assertThat(eventInsertionStrategyField, is(eventInsertionStrategy));

        final JdbcRepositoryHelper jdbcRepositoryHelperField = getValueOfField(eventJdbcRepository, "jdbcRepositoryHelper", JdbcRepositoryHelper.class);
        assertThat(jdbcRepositoryHelperField, is(jdbcRepositoryHelper));

        final JdbcDataSourceProvider jdbcDataSourceProviderField = getValueOfField(eventJdbcRepository, "jdbcDataSourceProvider", JdbcDataSourceProvider.class);
        assertThat(jdbcDataSourceProviderField, is(jdbcDataSourceProvider));

        final String jndiDatasourceField = getValueOfField(eventJdbcRepository, "jndiDatasource", String.class);
        assertThat(jndiDatasourceField, is(jndiDatasource));

        final Logger loggerField = getValueOfField(eventJdbcRepository, "logger", Logger.class);
        assertThat(loggerField, is(notNullValue()));
    }
}
