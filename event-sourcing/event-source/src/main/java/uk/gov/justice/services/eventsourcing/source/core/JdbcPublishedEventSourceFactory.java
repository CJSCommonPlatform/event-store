package uk.gov.justice.services.eventsourcing.source.core;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinderFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class JdbcPublishedEventSourceFactory {

    @Inject
    private EventConverter eventConverter;

    @Inject
    private PublishedEventFinderFactory publishedEventFinderFactory;

    @Inject
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    public DefaultPublishedEventSource create(final String jndiDatasource) {

        final DataSource dataSource = jdbcDataSourceProvider.getDataSource(jndiDatasource);
        final PublishedEventFinder publishedEventFinder = publishedEventFinderFactory.create(dataSource);

        return new DefaultPublishedEventSource(publishedEventFinder, eventConverter);
    }
}
