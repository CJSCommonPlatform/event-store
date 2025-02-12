package uk.gov.justice.services.event.buffer.core.repository.subscription;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorRepository;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StreamStatusJdbcRepositoryErrorHandlingIT {

    public static final String EVENT_LISTENER = "EVENT_LISTENER";
    protected static final long INITIAL_STREAM_POSITION = 0L;

    private DataSource dataSource = new FrameworkTestDataSourceFactory().createViewStoreDataSource();
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();
    private StreamStatusJdbcRepository streamStatusJdbcRepository = new StreamStatusJdbcRepository(dataSource, preparedStatementWrapperFactory);
    private final StreamErrorRepository streamErrorRepository = new StreamErrorRepository();

    @BeforeEach
    public void setup() {
        new DatabaseCleaner().cleanViewStoreTables(
                "framework",
                "stream_status",
                "stream_buffer",
                "stream_error");

        final DataSource viewStoreDataSource = new TestJdbcDataSourceProvider().getViewStoreDataSource("framework");

        final ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider = mock(ViewStoreJdbcDataSourceProvider.class);
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        setField(streamErrorRepository, "viewStoreDataSourceProvider", viewStoreJdbcDataSourceProvider);
    }

    @Test
    public void shouldInsertStreamIntoStreamStatusWithStreamErrorIdAndErrorPositionIfTheStreamDoesNotExistInStreamStatus() throws Exception {

        final UUID streamId = randomUUID();
        final UUID streamErrorId = randomUUID();
        final UUID eventId = randomUUID();
        final Long positionInStream = 23L;
        final String source = "source1";
        final String componentName = EVENT_LISTENER;
        final StreamError streamError = aStreamError(
                streamErrorId,
                eventId,
                streamId,
                positionInStream,
                componentName,
                source);

        streamErrorRepository.save(streamError);
        streamStatusJdbcRepository.markStreamAsErrored(streamId, streamErrorId, positionInStream, componentName, source);

        final Optional<StreamStatusErrorStatus> error = findErrorStatus(streamId, source, componentName);

        assertThat(error.isPresent(), is(true));
        assertThat(error.get().streamErrorId(), is(streamErrorId));
        assertThat(error.get().errorPositionInStream(), is(positionInStream));
        assertThat(error.get().positionInStream(), is(INITIAL_STREAM_POSITION));
    }

    @Test
    public void shouldUpdateStreamErrorIdAndErrorPositionInStreamStatusWhenTheStreamAlreadyExistsInStreamStatus() throws Exception {

        final UUID streamId = randomUUID();
        final UUID streamErrorId = randomUUID();
        final UUID eventId = randomUUID();

        final Long positionInStream = 23L;
        final String source = "source1";
        final String componentName = EVENT_LISTENER;
        final StreamError streamError = aStreamError(streamErrorId, eventId, streamId, positionInStream, componentName, source);

        streamErrorRepository.save(streamError);

        insertStreamIntoStreamStatusTable(streamId, positionInStream, source, componentName);

        streamStatusJdbcRepository.markStreamAsErrored(streamId, streamErrorId, positionInStream, componentName, source);

        final Optional<StreamStatusErrorStatus> error = findErrorStatus(streamId, source, componentName);

        assertThat(error.isPresent(), is(true));
        assertThat(error.get().streamErrorId(), is(streamErrorId));
        assertThat(error.get().errorPositionInStream(), is(positionInStream));
    }

    @Test
    public void shouldUnmarkStreamAsErrored() throws Exception {

        final UUID streamId = randomUUID();
        final UUID streamErrorId = randomUUID();
        final UUID eventId = randomUUID();
        final Long positionInStream = 23L;
        final String source = "source1";
        final String componentName = EVENT_LISTENER;
        final StreamError streamError = aStreamError(
                streamErrorId,
                eventId,
                streamId,
                positionInStream,
                componentName,
                source);

        streamErrorRepository.save(streamError);
        streamStatusJdbcRepository.markStreamAsErrored(streamId, streamErrorId, positionInStream, componentName, source);

        final Optional<StreamStatusErrorStatus> error = findErrorStatus(streamId, source, componentName);
        assertThat(error.isPresent(), is(true));
        assertThat(error.get().streamErrorId(), is(streamErrorId));
        assertThat(error.get().errorPositionInStream(), is(positionInStream));

        streamStatusJdbcRepository.unmarkStreamAsErrored(streamId, source, componentName);

        final Optional<StreamStatusErrorStatus> error2 = findErrorStatus(streamId, source, componentName);

        assertThat(error2.isPresent(), is(true));
        assertThat(error2.get().streamErrorId(), is(nullValue()));
        assertThat(error2.get().errorPositionInStream(), is(nullValue()));
    }

    @Test
    public void shouldHandleUnmarkingStreamAsErroredEvenIfNoStreamExistsInStreamStatusTable() throws Exception {

        final UUID streamId = randomUUID();
        final String source = "source1";
        final String componentName = EVENT_LISTENER;

        streamStatusJdbcRepository.unmarkStreamAsErrored(streamId, source, componentName);

        final Optional<StreamStatusErrorStatus> errorStatus = findErrorStatus(streamId, source, componentName);

        assertThat(errorStatus.isPresent(), is(false));
    }

    private StreamError aStreamError(
            final UUID streamErrorId,
            final UUID eventId,
            final UUID streamId,
            final Long positionInStream,
            final String componentName,
            final String source) {
        return new StreamError(
                streamErrorId,
                "hash",
                "some.exception.ClassName",
                "some-exception-message",
                empty(),
                empty(),
                "some.java.ClassName",
                "someMethod",
                2334,
                "events.context.some-event-name",
                eventId,
                streamId,
                positionInStream,
                new UtcClock().now(),
                "stack-trace",
                componentName,
                source
        );
    }

    private void insertStreamIntoStreamStatusTable(
            final UUID streamId,
            final Long positionInStream,
            final String source,
            final String componentName) throws SQLException {
        
        final String sql = "INSERT INTO stream_status (stream_id, position, source, component) VALUES (?, ?, ?, ?)";
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, streamId);
            preparedStatement.setLong(2, positionInStream);
            preparedStatement.setString(3, source);
            preparedStatement.setString(4, componentName);

            preparedStatement.executeUpdate();
        }
    }

    private Optional<StreamStatusErrorStatus> findErrorStatus(
            final UUID streamId,
            final String source,
            final String componentName) throws SQLException {
        final String sql = """
                SELECT
                    position,
                    stream_error_id,
                    stream_error_position
                FROM stream_status
                WHERE stream_id = ?
                AND source = ?
                AND component = ?""";

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, streamId);
            preparedStatement.setString(2, source);
            preparedStatement.setString(3, componentName);

            try(final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final Long foundPositionInStream = resultSet.getLong("position");
                    final UUID foundStreamErrorId = (UUID) resultSet.getObject("stream_error_id");
                    final Long foundStreamErrorPosition = (Long) resultSet.getObject("stream_error_position");

                    return of(new StreamStatusErrorStatus(
                            foundPositionInStream,
                            foundStreamErrorId,
                            foundStreamErrorPosition
                    ));
                }

                return empty();
            }
        }
    }
}

record StreamStatusErrorStatus(Long positionInStream, UUID streamErrorId, Long errorPositionInStream) {
}
