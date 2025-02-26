package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.transaction.Transactional.TxType.REQUIRED;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class StreamErrorRepository {

    @Inject
    private ViewStoreJdbcDataSourceProvider viewStoreDataSourceProvider;

    @Inject
    private StreamErrorHashPersistence streamErrorHashPersistence;

    @Inject
    private StreamErrorDetailsPersistence streamErrorDetailsPersistence;

    @Transactional(REQUIRED)
    public void save(final StreamError streamError) {
        try (final Connection connection = viewStoreDataSourceProvider.getDataSource().getConnection()) {
            streamErrorHashPersistence.upsert(streamError.streamErrorHash(), connection);
            streamErrorDetailsPersistence.insert(streamError.streamErrorDetails(), connection);
        } catch (final SQLException e) {
            throw new StreamErrorHandlingException(format("Failed to save StreamError: %s", streamError), e);
        }
    }

    @Transactional(REQUIRED)
    public Optional<StreamError> findBy(final UUID streamErrorId) {
        try (final Connection connection = viewStoreDataSourceProvider.getDataSource().getConnection()) {

            final Optional<StreamErrorDetails> streamErrorDetailsOptional = streamErrorDetailsPersistence.findBy(streamErrorId, connection);

            if (streamErrorDetailsOptional.isPresent()) {
                final StreamErrorDetails streamErrorDetails = streamErrorDetailsOptional.get();
                final Optional<StreamErrorHash> streamErrorHashOptional = streamErrorHashPersistence.findByHash(streamErrorDetails.hash(), connection);
                if (streamErrorHashOptional.isPresent()) {
                    final StreamError streamError = new StreamError(streamErrorDetails, streamErrorHashOptional.get());
                    return of(streamError);
                }
            }

            return empty();

        } catch (final SQLException e) {
            throw new StreamErrorHandlingException(format("Failed find StreamError by streamErrorId: '%s'", streamErrorId), e);
        }
    }

    @Transactional(REQUIRED)
    public void removeErrorForStream(final UUID streamId, final String source, final String componentName) {

        try(final Connection connection = viewStoreDataSourceProvider.getDataSource().getConnection()) {
            streamErrorDetailsPersistence.deleteBy(streamId, source, componentName, connection);
            streamErrorHashPersistence.deleteOrphanedHashes(connection);
        } catch (final SQLException e) {
            throw new StreamErrorHandlingException(format(
                    "Failed to remove error for stream. streamId: '%s', source: '%s, component: '%s'",
                    streamId,
                    source,
                    componentName
            ), e);
        }
    }
}
