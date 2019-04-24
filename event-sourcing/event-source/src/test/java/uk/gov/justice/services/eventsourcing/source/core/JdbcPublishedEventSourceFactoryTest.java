package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinderFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JdbcPublishedEventSourceFactoryTest {

    @Mock
    private EventConverter eventConverter;

    @Mock
    private PublishedEventFinderFactory publishedEventFinderFactory;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @InjectMocks
    private JdbcPublishedEventSourceFactory jdbcPublishedEventSourceFactory;

    @Test
    public void shouldCreateJdbcBasedPublishedEventSource() throws Exception {

        final String jndiDatasource = "jndiDatasource";

        final DataSource dataSource = jdbcDataSourceProvider.getDataSource(jndiDatasource);
        final PublishedEventFinder publishedEventFinder = publishedEventFinderFactory.create(dataSource);

        when(jdbcDataSourceProvider.getDataSource(jndiDatasource)).thenReturn(dataSource);
        when(publishedEventFinderFactory.create(dataSource)).thenReturn(publishedEventFinder);


        final DefaultPublishedEventSource defaultPublishedEventSource = jdbcPublishedEventSourceFactory.create(jndiDatasource);

        assertThat(getValueOfField(defaultPublishedEventSource, "publishedEventFinder", PublishedEventFinder.class), is(publishedEventFinder));
        assertThat(getValueOfField(defaultPublishedEventSource, "eventConverter", EventConverter.class), is(eventConverter));
    }
}
