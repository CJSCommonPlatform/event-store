package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import javax.inject.Inject;
import javax.sql.DataSource;

public class MultipleDataSourcePublishedEventRepositoryFactory {

    @Inject
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    public MultipleDataSourcePublishedEventRepository create(final DataSource dataSource) {
        return new MultipleDataSourcePublishedEventRepository(
                jdbcResultSetStreamer,
                preparedStatementWrapperFactory,
                dataSource);
    }
}
