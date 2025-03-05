package uk.gov.justice.services.event.buffer.core.repository.subscription;

import static java.lang.String.format;
import static javax.transaction.Transactional.TxType.REQUIRED;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;

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
    private static final String SELECT_BY_STREAM_ID_AND_SOURCE_SQL = "SELECT stream_id, position, source, component FROM stream_status WHERE stream_id=? AND component=? AND source in (?,'unknown') FOR UPDATE";
    private static final String INSERT_SQL = "INSERT INTO stream_status (position, stream_id, source, component, updated_at) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_ON_CONFLICT_DO_NOTHING_SQL = INSERT_SQL + " ON CONFLICT DO NOTHING";
    private static final String UPDATE_SQL = "UPDATE stream_status SET position=?,source=?,component=? WHERE stream_id=? and component=? and source in (?,'unknown')";
    private static final String UPDATE_UNKNOWN_SOURCE_SQL = "UPDATE stream_status SET source=?, component=? WHERE stream_id=? and source = 'unknown'";

    private final String UPSERT_STREAM_ERROR_SQL = """
            INSERT INTO stream_status (
                stream_id,
                position,
                source,
                component,
                stream_error_id,
                stream_error_position,
                updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (stream_id, source, component)
            DO UPDATE
            SET stream_error_id = ?, stream_error_position = ?, updated_at = ?""";

    private static final long INITIAL_POSITION_ON_ERROR = 0L;

    @Inject
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @Inject
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Inject
    private UtcClock clock;

    public StreamStatusJdbcRepository() {
    }

    public StreamStatusJdbcRepository(
            final ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider,
            final PreparedStatementWrapperFactory preparedStatementWrapperFactory,
            final UtcClock clock) {
        this.viewStoreJdbcDataSourceProvider = viewStoreJdbcDataSourceProvider;
        this.preparedStatementWrapperFactory = preparedStatementWrapperFactory;
        this.clock = clock;
    }


    /**
     * Insert the given Subscription into the subscription table.
     *
     * @param subscription the status of the stream to insert
     */
    public void insert(final Subscription subscription) {
        final DataSource viewStoreDataSource = viewStoreJdbcDataSourceProvider.getDataSource();
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(viewStoreDataSource, INSERT_SQL)) {
            ps.setLong(1, subscription.getPosition());
            ps.setObject(2, subscription.getStreamId());
            ps.setString(3, subscription.getSource());
            ps.setString(4, subscription.getComponent());
            ps.setTimestamp(5, toSqlTimestamp(clock.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream: %s", subscription), e);
        }
    }


    /**
     * Tries to insert if database is PostgresSQL and version&gt;=9.5. Uses PostgreSQl-specific sql
     * clause. Does not fail if status for the given stream already exists
     *
     * @param subscription the status of the stream to insert
     */
    public void insertOrDoNothing(final Subscription subscription) {

        final DataSource viewStoreDataSource = viewStoreJdbcDataSourceProvider.getDataSource();
        try (final PreparedStatementWrapper preparedStatement = preparedStatementWrapperFactory.preparedStatementWrapperOf(viewStoreDataSource, INSERT_ON_CONFLICT_DO_NOTHING_SQL)) {
            preparedStatement.setLong(1, subscription.getPosition());
            preparedStatement.setObject(2, subscription.getStreamId());
            preparedStatement.setString(3, subscription.getSource());
            preparedStatement.setString(4, subscription.getComponent());
            preparedStatement.setTimestamp(5, toSqlTimestamp(clock.now()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while storing status of the stream in PostgreSQL: %s", subscription), e);
        }

    }

    /**
     * Insert the given Subscription into the stream status table.
     *
     * @param subscription the event to insert
     */
    public void update(final Subscription subscription) {

        final DataSource dataSource = viewStoreJdbcDataSourceProvider.getDataSource();
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, UPDATE_SQL)) {
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
     * @param streamId  streamId of the stream.
     * @param component
     * @return a {@link Subscription}.
     */
    public Optional<Subscription> findByStreamIdAndSource(final UUID streamId, final String source, final String component) {

        final DataSource dataSource = viewStoreJdbcDataSourceProvider.getDataSource();
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SELECT_BY_STREAM_ID_AND_SOURCE_SQL)) {
            ps.setObject(1, streamId);
            ps.setObject(2, component);
            ps.setObject(3, source);
            return subscriptionFrom(ps);

        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while looking up status of the stream: %s", streamId), e);
        }
    }

    private Optional<Subscription> subscriptionFrom(final PreparedStatementWrapper ps) throws SQLException {
        final ResultSet resultSet = ps.executeQuery();
        return resultSet.next()
                ? Optional.of(entityFrom(resultSet))
                : Optional.empty();

    }

    protected Subscription entityFrom(final ResultSet rs) throws SQLException {
        return new Subscription((UUID) rs.getObject(PRIMARY_KEY_ID), rs.getLong(LATEST_POSITION_COLUMN), rs.getString(SOURCE), rs.getString(COMPONENT));
    }

    public void updateSource(final UUID streamId, final String source, final String component) {
        final DataSource dataSource = viewStoreJdbcDataSourceProvider.getDataSource();
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, UPDATE_UNKNOWN_SOURCE_SQL)) {
            ps.setString(1, source);
            ps.setObject(2, component);
            ps.setObject(3, streamId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while updating unknown source of the stream: %s", streamId), e);
        }
    }

    @Transactional(REQUIRED)
    public void markStreamAsErrored(
            final UUID streamId,
            final UUID streamErrorId,
            final Long errorPosition,
            final String componentName,
            final String source) {

        final ZonedDateTime updatedAt = clock.now();

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(UPSERT_STREAM_ERROR_SQL)) {

            final Timestamp updatedAtTimestamp = toSqlTimestamp(updatedAt);

            preparedStatement.setObject(1, streamId);
            preparedStatement.setLong(2, INITIAL_POSITION_ON_ERROR);
            preparedStatement.setString(3, source);
            preparedStatement.setString(4, componentName);
            preparedStatement.setObject(5, streamErrorId);
            preparedStatement.setLong(6, errorPosition);
            preparedStatement.setTimestamp(7, updatedAtTimestamp);
            preparedStatement.setObject(8, streamErrorId);
            preparedStatement.setLong(9, errorPosition);
            preparedStatement.setTimestamp(10, updatedAtTimestamp);

            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(
                    format("Failed to mark stream as errored in stream_status table. streamId: '%s', component: '%s', streamErrorId: '%s' positionInStream: %s",
                            streamId,
                            componentName,
                            streamErrorId,
                            errorPosition),
                    e);
        }
    }

    @Transactional(REQUIRED)
    public void unmarkStreamAsErrored(final UUID streamId, final String source, final String componentName) {

        final String UNMARK_STREAM_AS_ERRORED_SQL = """
                    UPDATE stream_status
                    SET stream_error_id = NULL,
                        stream_error_position = NULL
                    WHERE stream_id = ?
                    AND source = ?
                    AND component = ?
                """;

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(UNMARK_STREAM_AS_ERRORED_SQL)) {
            preparedStatement.setObject(1, streamId);
            preparedStatement.setString(2, source);
            preparedStatement.setString(3, componentName);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new JdbcRepositoryException(format("Failed to unmark stream as errored in stream_status table. streamId: '%s'", streamId), e);
        }
    }
}
