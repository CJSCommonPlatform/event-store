package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.sql.Connection;
import java.util.List;
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

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreDataSourceProvider;

    @Spy
    private StreamErrorHashPersistence streamErrorHashPersistence = new StreamErrorHashPersistence();

    @Spy
    private StreamErrorDetailsPersistence streamErrorDetailsPersistence = new StreamErrorDetailsPersistence();

    private final DataSource viewStoreDataSource = new TestJdbcDataSourceProvider().getViewStoreDataSource("framework");
    final DatabaseCleaner databaseCleaner = new DatabaseCleaner();


    @BeforeEach
    public void cleanTables() {
        databaseCleaner.cleanViewStoreTables("framework", "stream_error_hash", "stream_error");
    }

    @InjectMocks
    private StreamErrorRepository streamErrorRepository;

    @Test
    public void shouldSaveAndRemoveErrorsCorrectly() throws Exception {

        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        final UUID streamId = randomUUID();
        final String hash = "this-is-a-hash";
        final String source = "this-is-the-source";
        final String componentName_1 = "EVENT_LISTENER";
        final String componentName_2 = "EVENT_INDEXER";

        // add 2 errors which both have the same hash
        final StreamErrorDetails streamErrorDetails_1 = aStreamErrorDetails(streamId, hash, componentName_1, source);
        final StreamErrorDetails streamErrorDetails_2 = aStreamErrorDetails(streamId, hash, componentName_2, source);
        final StreamErrorHash streamErrorHash = aStreamErrorHash(hash);

        streamErrorRepository.save(new StreamError(streamErrorDetails_1, streamErrorHash));
        streamErrorRepository.save(new StreamError(streamErrorDetails_2, streamErrorHash));

        // check everything was saved
        try (final Connection connection = viewStoreDataSource.getConnection()) {

            final Optional<StreamErrorHash> optionalStreamErrorHash = streamErrorHashPersistence.findByHash(hash, connection);
            assertThat(optionalStreamErrorHash, is(of(streamErrorHash)));
            final List<StreamErrorDetails> streamErrorDetails = streamErrorDetailsPersistence.findAll(connection);

            assertThat(streamErrorDetails.size(), is(2));
            assertThat(streamErrorDetails.get(0), is(streamErrorDetails_1));
            assertThat(streamErrorDetails.get(1), is(streamErrorDetails_2));
        }

        // remove one of the errors
        streamErrorRepository.removeErrorForStream(streamId, source, componentName_2);

        try (final Connection connection = viewStoreDataSource.getConnection()) {
            final List<StreamErrorDetails> streamErrorDetails = streamErrorDetailsPersistence.findAll(connection);

            // now only one error remaining
            assertThat(streamErrorDetails.size(), is(1));
            assertThat(streamErrorDetails.get(0), is(streamErrorDetails_1));

            // but the hash wasn't deleted
            final Optional<StreamErrorHash> optionalStreamErrorHash = streamErrorHashPersistence.findByHash(hash, connection);
            assertThat(optionalStreamErrorHash, is(of(streamErrorHash)));
        }

        // remove the final error
        streamErrorRepository.removeErrorForStream(streamId, source, componentName_1);

        try (final Connection connection = viewStoreDataSource.getConnection()) {

            // now no errors remaining
            final List<StreamErrorDetails> streamErrorDetails = streamErrorDetailsPersistence.findAll(connection);
            assertThat(streamErrorDetails.isEmpty(), is(true));
            // and the hash has also been deleted
            final Optional<StreamErrorHash> optionalStreamErrorHash = streamErrorHashPersistence.findByHash(hash, connection);
            assertThat(optionalStreamErrorHash, is(empty()));
        }
    }

    private StreamErrorDetails aStreamErrorDetails(
            final UUID streamId,
            final String hash,
            final String componentName,
            final String source) {

        return new StreamErrorDetails(
                randomUUID(),
                hash,
                "some-exception-message",
                empty(),
                "event-name",
                randomUUID(),
                streamId,
                234L,
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
}