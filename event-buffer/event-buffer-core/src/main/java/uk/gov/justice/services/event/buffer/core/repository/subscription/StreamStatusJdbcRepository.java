package uk.gov.justice.services.event.buffer.core.repository.subscription;

import static java.lang.String.format;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class StreamStatusJdbcRepository {

    /**
     * Column Names
     */
    private static final String PRIMARY_KEY_ID = "stream_id";
    private static final String LATEST_POSITION_COLUMN = "position";
    private static final String SOURCE = "source";
    private static final String COMPONENT = "component";

    /**
     * Statements
     */
    private static final String SELECT_BY_STREAM_ID_AND_SOURCE = "SELECT stream_id, position, source, component FROM stream_status WHERE stream_id=? AND component=? AND source in (?,'unknown') FOR UPDATE";
    private static final String INSERT = "INSERT INTO stream_status (position, stream_id, source, component) VALUES (?, ?, ?, ?)";
    private static final String INSERT_ON_CONFLICT_DO_NOTHING = INSERT + " ON CONFLICT DO NOTHING";
    private static final String UPDATE = "UPDATE stream_status SET position=?,source=?,component=? WHERE stream_id=? and component=? and source in (?,'unknown')";
    private static final String UPDATE_UNKNOWN_SOURCE = "UPDATE stream_status SET source=?, component=? WHERE stream_id=? and source = 'unknown'";

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Inject
    private ViewStoreJdbcDataSourceProvider dataSourceProvider;

    private DataSource dataSource;

    public StreamStatusJdbcRepository() {}

    public StreamStatusJdbcRepository(final DataSource dataSource, final PreparedStatementWrapperFactory preparedStatementWrapperFactory) {
        this.dataSource = dataSource;
        this.preparedStatementWrapperFactory = preparedStatementWrapperFactory;
    }

    @PostConstruct
    private void initialiseDataSource() {
        dataSource = dataSourceProvider.getDataSource();
    }


    /**
     * Insert the given Subscription into the subscription table.
     *
     * @param subscription the status of the stream to insert
     */
    public void insert(final Subscription subscription) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, INSERT)) {
            ps.setLong(1, subscription.getPosition());
            ps.setObject(2, subscription.getStreamId());
            ps.setString(3, subscription.getSource());
            ps.setString(4, subscription.getComponent());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream: %s", subscription), e);
        }
    }


    /**
     * Tries to insert if database is PostgresSQL and version&gt;=9.5. Uses PostgreSQl-specific sql
     * clause. Does not fail if status for the given stream already exists
     *  @param subscription the status of the stream to insert
     *
     */
    public void insertOrDoNothing(final Subscription subscription) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, INSERT_ON_CONFLICT_DO_NOTHING)) {
            ps.setLong(1, subscription.getPosition());
            ps.setObject(2, subscription.getStreamId());
            ps.setString(3, subscription.getSource());
            ps.setString(4, subscription.getComponent());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream in PostgreSQL: %s", subscription), e);
        }

    }

    /**
     * Insert the given Subscription into the stream status table.
     *  @param subscription the event to insert
     *
     */
    public void update(final Subscription subscription) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, UPDATE)) {
            ps.setLong(1, subscription.getPosition());
            ps.setString(2, subscription.getSource());
            ps.setObject(3, subscription.getComponent());
            ps.setObject(4, subscription.getStreamId());
            ps.setObject(5, subscription.getComponent());
            ps.setString(6, subscription.getSource());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while updating status of the stream: %s", subscription), e);
        }
    }

    /**
     * Returns a Stream of {@link Subscription} for the given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @param component
     * @return a {@link Subscription}.
     */
    public Optional<Subscription> findByStreamIdAndSource(final UUID streamId, final String source, final String component) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SELECT_BY_STREAM_ID_AND_SOURCE)) {
            ps.setObject(1, streamId);
            ps.setObject(2, component);
            ps.setObject(3, source);
            return subscriptionFrom(ps);

        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while looking up status of the stream: %s", streamId), e);
        }
    }

    private Optional<Subscription> subscriptionFrom(final PreparedStatementWrapper ps) throws SQLException {
        try (final ResultSet resultSet = ps.executeQuery()) {
            return resultSet.next()
                    ? Optional.of(entityFrom(resultSet))
                    : Optional.empty();
        }
    }

    protected Subscription entityFrom(final ResultSet rs) throws SQLException {
        return new Subscription((UUID) rs.getObject(PRIMARY_KEY_ID), rs.getLong(LATEST_POSITION_COLUMN), rs.getString(SOURCE), rs.getString(COMPONENT));
    }

    public void updateSource(final UUID streamId, final String source, final String component) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, UPDATE_UNKNOWN_SOURCE)) {
            ps.setString(1, source);
            ps.setObject(2, component);
            ps.setObject(3, streamId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while updating unknown source of the stream: %s", streamId), e);
        }
    }
}
