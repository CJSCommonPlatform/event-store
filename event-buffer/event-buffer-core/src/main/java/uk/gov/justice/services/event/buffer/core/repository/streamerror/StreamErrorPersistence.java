package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

public class StreamErrorPersistence {

    @Inject
    private StreamErrorHashPersistence streamErrorHashPersistence;

    @Inject
    private StreamErrorDetailsPersistence streamErrorDetailsPersistence;

    public boolean save(final StreamError streamError, final Connection connection) {

        try {
            streamErrorHashPersistence.upsert(streamError.streamErrorHash(), connection);
            final int rowsUpdated = streamErrorDetailsPersistence.insert(streamError.streamErrorDetails(), connection);
            return rowsUpdated > 0;
        } catch (final SQLException e) {
            throw new StreamErrorHandlingException(format("Failed to save StreamError: %s", streamError), e);
        }
    }

    public Optional<StreamError> findBy(final UUID streamErrorId, final Connection connection) {
        try  {

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

    public void removeErrorForStream(final UUID streamId, final String source, final String componentName, final Connection connection) {

        try {
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
