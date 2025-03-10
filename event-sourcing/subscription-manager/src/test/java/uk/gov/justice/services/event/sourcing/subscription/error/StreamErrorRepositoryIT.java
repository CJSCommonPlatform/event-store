package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorDetails;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorDetailsPersistence;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorHash;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorHashPersistence;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorPersistence;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamStatusErrorPersistence;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamErrorRepositoryIT {

    private final DataSource viewStoreDataSource = new TestJdbcDataSourceProvider().getViewStoreDataSource("framework");

    @Spy
    private StreamErrorHashPersistence streamErrorHashPersistence = new StreamErrorHashPersistence();

    @Spy
    private StreamErrorDetailsPersistence streamErrorDetailsPersistence = new StreamErrorDetailsPersistence();

    @Spy
    private StreamErrorPersistence streamErrorPersistence = new StreamErrorPersistence();

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider = new ViewStoreJdbcDataSourceProvider();

    @Spy
    private StreamStatusErrorPersistence streamStatusErrorPersistence = new StreamStatusErrorPersistence();

    @InjectMocks
    private StreamErrorRepository streamErrorRepository;

    @BeforeEach
    public void setup() {
        new DatabaseCleaner().cleanViewStoreTables(
                "framework",
                "stream_status",
                "stream_buffer",
                "stream_error");


        setField(streamErrorPersistence, "streamErrorHashPersistence", streamErrorHashPersistence);
        setField(streamErrorPersistence, "streamErrorDetailsPersistence", streamErrorDetailsPersistence);
        setField(streamStatusErrorPersistence, "clock", new UtcClock());
    }

    @Test
    public void shouldSaveNewStreamErrorAndUpdateStreamStatusTable() throws Exception {

        final long streamErrorPosition = 234L;

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        final StreamError streamError = aStreamError(streamErrorPosition);
        streamErrorRepository.markStreamAsErrored(streamError);

        try (final Connection connection = viewStoreDataSource.getConnection()) {
            final Optional<StreamError> streamErrorOptional = streamErrorPersistence.findBy(streamError.streamErrorDetails().id(), connection);
            assertThat(streamErrorOptional, is(of(streamError)));
        }

        final Optional<StreamStatusErrorDetails> streamStatusErrorDetails = findErrorInStreamStatusTable(streamError.streamErrorDetails().id());

        if (streamStatusErrorDetails.isPresent()) {
            assertThat(streamStatusErrorDetails.get().streamErrorId, is(streamError.streamErrorDetails().id()));
            assertThat(streamStatusErrorDetails.get().streamId, is(streamError.streamErrorDetails().streamId()));
            assertThat(streamStatusErrorDetails.get().streamErrorPosition, is(streamErrorPosition));
            assertThat(streamStatusErrorDetails.get().source, is(streamError.streamErrorDetails().source()));
            assertThat(streamStatusErrorDetails.get().component, is(streamError.streamErrorDetails().componentName()));
        } else {
            fail();
        }
    }


    @Test
    public void shouldSaveNewStreamErrorAndUpdateStreamStatusTableWhenStreamExistsInStreamStatusTable() throws Exception {

        final long streamErrorPosition = 234L;
        final StreamError streamError = aStreamError(streamErrorPosition);

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        final String sql = """
                INSERT INTO stream_status (stream_id, position, source, component, updated_at)
                VALUES(?, ?, ?, ?, ?)
            """;
        try(final Connection connection = viewStoreDataSource.getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, streamError.streamErrorDetails().streamId());
            preparedStatement.setLong(2, 1L);
            preparedStatement.setString(3, streamError.streamErrorDetails().source());
            preparedStatement.setString(4, streamError.streamErrorDetails().componentName());
            preparedStatement.setTimestamp(5, toSqlTimestamp(new UtcClock().now()));
            preparedStatement.executeUpdate();
        }

        streamErrorRepository.markStreamAsErrored(streamError);

        try (final Connection connection = viewStoreDataSource.getConnection()) {
            final Optional<StreamError> streamErrorOptional = streamErrorPersistence.findBy(streamError.streamErrorDetails().id(), connection);
            assertThat(streamErrorOptional, is(of(streamError)));
        }

        final Optional<StreamStatusErrorDetails> streamStatusErrorDetails = findErrorInStreamStatusTable(streamError.streamErrorDetails().id());

        if (streamStatusErrorDetails.isPresent()) {
            assertThat(streamStatusErrorDetails.get().streamErrorId, is(streamError.streamErrorDetails().id()));
            assertThat(streamStatusErrorDetails.get().streamId, is(streamError.streamErrorDetails().streamId()));
            assertThat(streamStatusErrorDetails.get().streamErrorPosition, is(streamErrorPosition));
            assertThat(streamStatusErrorDetails.get().source, is(streamError.streamErrorDetails().source()));
            assertThat(streamStatusErrorDetails.get().component, is(streamError.streamErrorDetails().componentName()));
        } else {
            fail();
        }
    }

    private Optional<StreamStatusErrorDetails> findErrorInStreamStatusTable(final UUID streamErrorId) throws SQLException {

        final String sql = """
                SELECT
                    stream_id,
                    stream_error_position,
                    source,
                    component
                FROM stream_status
                WHERE stream_error_id = ?
                """;

        try (final Connection connection = viewStoreDataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, streamErrorId);
            try(final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    final UUID streamId = (UUID) resultSet.getObject("stream_id");
                    final long streamErrorPosition = resultSet.getLong("stream_error_position");
                    final String source = resultSet.getString("source");
                    final String component = resultSet.getString("component");

                    final StreamStatusErrorDetails streamStatusErrorDetails = new StreamStatusErrorDetails(
                            streamErrorId,
                            streamId,
                            streamErrorPosition,
                            source,
                            component
                    );

                    return of(streamStatusErrorDetails);
                }
            }

            return empty();
        }
    }

    private StreamError aStreamError(long streamErrorPosition) {
        final String hash = "some-hash";
        return new StreamError(aStreamErrorDetails(hash, streamErrorPosition), aStreamErrorHash(hash));
    }

    private StreamErrorDetails aStreamErrorDetails(final String hash, final long streamErrorPosition) {

        final UUID streamId = randomUUID();
        final String componentName = "some-component";
        final String source = "some-source";

        return new StreamErrorDetails(
                randomUUID(),
                hash,
                "some-exception-message",
                empty(),
                "event-name",
                randomUUID(),
                streamId,
                streamErrorPosition,
                new UtcClock().now(),
                "stack-trace",
                componentName,
                source
        );
    }

    private StreamErrorHash aStreamErrorHash(final String hash) {
        return new StreamErrorHash(
                hash,
                "exception-class-name",
                empty(),
                "java-class-name",
                "java-method",
                23
        );
    }

    record StreamStatusErrorDetails(
            UUID streamErrorId,
            UUID streamId,
            long streamErrorPosition,
            String source,
            String component) {
    }
}