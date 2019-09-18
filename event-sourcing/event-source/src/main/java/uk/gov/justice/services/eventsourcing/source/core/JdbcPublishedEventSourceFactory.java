package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class JdbcPublishedEventSourceFactory {

    @Inject
    private MultipleDataSourcePublishedEventRepositoryFactory multipleDataSourcePublishedEventRepositoryFactory;

    @Inject
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    public DefaultPublishedEventSource create(final String jndiDatasource) {

        final DataSource dataSource = jdbcDataSourceProvider.getDataSource(jndiDatasource);
        final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository = multipleDataSourcePublishedEventRepositoryFactory.create(dataSource);

        return new DefaultPublishedEventSource(multipleDataSourcePublishedEventRepository);
    }
}
