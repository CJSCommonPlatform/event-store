package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static javax.transaction.Transactional.TxType.REQUIRED;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromSqlTimestamp;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class StreamErrorRepository {

    private static final String INSERT_EXCEPTION_SQL = """
            INSERT INTO stream_error (
                id,
                hash,
                exception_classname,
                exception_message,
                cause_classname,
                cause_message,
                java_classname,
                java_method,
                java_line_number,
                event_name,
                event_id,
                stream_id,
                position_in_stream,
                date_created,
                full_stack_trace,
                component_name,
                source
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
                hash,
                exception_classname,
                exception_message,
                cause_classname,
                cause_message,
                java_classname,
                java_method,
                java_line_number,
                event_name,
                event_id,
                stream_id,
                position_in_stream,
                date_created,
                full_stack_trace,
                component_name,
                source
            FROM stream_error
            WHERE id = ?
            """;

    private static final String REMOVE_ERROR_ON_STREAM_SQL = "DELETE FROM stream_error WHERE stream_id = ? AND source = ? AND component_name = ?";

    @Inject
    private ViewStoreJdbcDataSourceProvider viewStoreDataSourceProvider;

    @Transactional(REQUIRED)
    public void save(final StreamError streamError) {

        try (final Connection connection = viewStoreDataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_EXCEPTION_SQL)) {

            final UUID id = streamError.id();
            final String hash = streamError.hash();
            final String exceptionClassName = streamError.exceptionClassName();
            final String exceptionMessage = streamError.exceptionMessage();
            final Optional<String> causeClassName = streamError.causeClassName();
            final Optional<String> causeMessage = streamError.causeMessage();
            final String javaClassname = streamError.javaClassname();
            final String javaMethod = streamError.javaMethod();
            final int javaLineNumber = streamError.javaLineNumber();
            final String eventName = streamError.eventName();
            final UUID eventId = streamError.eventId();
            final UUID streamId = streamError.streamId();
            final Long positionInStream = streamError.positionInStream();
            final ZonedDateTime dateCreated = streamError.dateCreated();
            final String fullStackTrace = streamError.fullStackTrace();
            final String componentName = streamError.componentName();
            final String source = streamError.source();

            preparedStatement.setObject(1, id);
            preparedStatement.setString(2, hash);
            preparedStatement.setString(3, exceptionClassName);
            preparedStatement.setString(4, exceptionMessage);
            preparedStatement.setString(5, causeClassName.orElse(null));
            preparedStatement.setString(6, causeMessage.orElse(null));
            preparedStatement.setString(7, javaClassname);
            preparedStatement.setString(8, javaMethod);
            preparedStatement.setInt(9, javaLineNumber);
            preparedStatement.setString(10, eventName);
            preparedStatement.setObject(11, eventId);
            preparedStatement.setObject(12, streamId);
            preparedStatement.setLong(13, positionInStream);
            preparedStatement.setTimestamp(14, toSqlTimestamp(dateCreated));
            preparedStatement.setString(15, fullStackTrace);
            preparedStatement.setString(16, componentName);
            preparedStatement.setString(17, source);

            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            throw new StreamErrorHandlingException("Failed to insert into 'stream_error' table", e);
        }
    }

    @Transactional(REQUIRED)
    public Optional<StreamError> findBy(final UUID id) {

        try (final Connection connection = viewStoreDataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            preparedStatement.setObject(1, id);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    final String hash = resultSet.getString("hash");
                    final String exceptionClassName = resultSet.getString("exception_classname");
                    final String exceptionMessage = resultSet.getString("exception_message");
                    final String causeClassName = resultSet.getString("cause_classname");
                    final String causeMessage = resultSet.getString("cause_message");
                    final String javaClassname = resultSet.getString("java_classname");
                    final String javaMethod = resultSet.getString("java_method");
                    final int javaLineNumber = resultSet.getInt("java_line_number");
                    final String eventName = resultSet.getString("event_name");
                    final UUID eventId = (UUID) resultSet.getObject("event_id");
                    final UUID streamId = (UUID) resultSet.getObject("stream_id");
                    final Long positionInStream = resultSet.getLong("position_in_stream");
                    final ZonedDateTime dateCreated = fromSqlTimestamp(resultSet.getTimestamp("date_created"));
                    final String fullStackTrace = resultSet.getString("full_stack_trace");
                    final String componentName = resultSet.getString("component_name");;
                    final String source = resultSet.getString("source");;

                    final StreamError streamError = new StreamError(
                            id,
                            hash,
                            exceptionClassName,
                            exceptionMessage,
                            ofNullable(causeClassName),
                            ofNullable(causeMessage),
                            javaClassname,
                            javaMethod,
                            javaLineNumber,
                            eventName,
                            eventId,
                            streamId,
                            positionInStream,
                            dateCreated,
                            fullStackTrace,
                            componentName,
                            source
                    );

                    return of(streamError);
                }

                return empty();
            }
        } catch (final SQLException e) {
            throw new StreamErrorHandlingException("Failed to read from 'stream_error' table", e);
        }
    }
    
    @Transactional(REQUIRED)
    public int removeErrorForStream(final UUID streamId, final String source, final String componentName) {

        try (final Connection connection = viewStoreDataSourceProvider.getDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(REMOVE_ERROR_ON_STREAM_SQL)) {
            preparedStatement.setObject(1, streamId);
            preparedStatement.setString(2, source);
            preparedStatement.setString(3, componentName);
            return preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new StreamErrorHandlingException(format("Failed delete errors by stream id '%s' from 'stream_error' table", streamId), e);
        }
    }
}
