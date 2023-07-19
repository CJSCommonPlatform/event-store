package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JdbcPublishedEventSourceFactoryTest {

    @Mock
    private MultipleDataSourcePublishedEventRepositoryFactory multipleDataSourcePublishedEventRepositoryFactory;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @InjectMocks
    private JdbcPublishedEventSourceFactory jdbcPublishedEventSourceFactory;

    @Test
    public void shouldCreateJdbcBasedPublishedEventSource() throws Exception {

        final String jndiDatasource = "jndiDatasource";

        final DataSource dataSource = jdbcDataSourceProvider.getDataSource(jndiDatasource);
        final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository = multipleDataSourcePublishedEventRepositoryFactory.create(dataSource);

        when(jdbcDataSourceProvider.getDataSource(jndiDatasource)).thenReturn(dataSource);
        when(multipleDataSourcePublishedEventRepositoryFactory.create(dataSource)).thenReturn(multipleDataSourcePublishedEventRepository);

        final DefaultPublishedEventSource defaultPublishedEventSource = jdbcPublishedEventSourceFactory.create(jndiDatasource);

        assertThat(getValueOfField(defaultPublishedEventSource, "multipleDataSourcePublishedEventRepository", MultipleDataSourcePublishedEventRepository.class), is(multipleDataSourcePublishedEventRepository));
    }
}
