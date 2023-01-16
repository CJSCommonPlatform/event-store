package uk.gov.justice.services.eventsourcing.repository.jdbc.datasource;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventStoreDataSourceProviderTest {

    @Mock
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @InjectMocks
    private DefaultEventStoreDataSourceProvider defaultEventStoreDataSourceProvider;

    @Test
    public void shouldLookupADataSourceUsingTheDefaultEventSourceName() throws Exception {

        final String datasourceJndiName = "datasource jndi name";

        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Location location = mock(Location.class);
        final DataSource dataSource = mock(DataSource.class);

        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(location.getDataSource()).thenReturn(of(datasourceJndiName));
        when(jdbcDataSourceProvider.getDataSource(datasourceJndiName)).thenReturn(dataSource);

        assertThat(defaultEventStoreDataSourceProvider.getDefaultDataSource(), is(dataSource));
    }

    @Test
    public void shouldCacheTheDataSourceOnceLookedUp() throws Exception {

        final String datasourceJndiName = "datasource jndi name";

        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Location location = mock(Location.class);
        final DataSource dataSource = mock(DataSource.class);

        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(location.getDataSource()).thenReturn(of(datasourceJndiName));
        when(jdbcDataSourceProvider.getDataSource(datasourceJndiName)).thenReturn(dataSource);

        defaultEventStoreDataSourceProvider.getDefaultDataSource();
        defaultEventStoreDataSourceProvider.getDefaultDataSource();
        defaultEventStoreDataSourceProvider.getDefaultDataSource();
        defaultEventStoreDataSourceProvider.getDefaultDataSource();
        defaultEventStoreDataSourceProvider.getDefaultDataSource();
        defaultEventStoreDataSourceProvider.getDefaultDataSource();
        defaultEventStoreDataSourceProvider.getDefaultDataSource();

        verify(jdbcDataSourceProvider, times(1)).getDataSource(datasourceJndiName);
    }

    @Test
    public void shouldThrowExceptionIfNoDefaultDataSourceFound() throws Exception {

        final EventSourceDefinition eventSourceDefinition = mock(EventSourceDefinition.class);
        final Location location = mock(Location.class);

        when(eventSourceDefinition.getLocation()).thenReturn(location);
        when(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition()).thenReturn(eventSourceDefinition);
        when(location.getDataSource()).thenReturn(empty());

        try {
            defaultEventStoreDataSourceProvider.getDefaultDataSource();
            fail();
        } catch (final MissingEventStoreDataSourceName expected) {
            assertThat(expected.getMessage(), is("No data_source specified in 'event-sources.yaml' marked as default"));
        }
    }

    @Test
    public void shouldGetNamedDataSource() throws Exception {

        final String datasourceJndiName = "datasource jndi name";

        final DataSource dataSource = mock(DataSource.class);

        when(jdbcDataSourceProvider.getDataSource(datasourceJndiName)).thenReturn(dataSource);

        assertThat(defaultEventStoreDataSourceProvider.getDataSource(datasourceJndiName), is(dataSource));
    }
}
