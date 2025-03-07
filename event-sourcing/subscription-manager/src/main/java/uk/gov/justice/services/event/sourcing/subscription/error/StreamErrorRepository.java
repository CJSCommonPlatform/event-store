package uk.gov.justice.services.event.sourcing.subscription.error;

import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorDetails;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorHandlingException;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorPersistence;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamStatusErrorPersistence;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class StreamErrorRepository {

    @Inject
    private StreamErrorPersistence streamErrorPersistence;

    @Inject
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Inject
    private StreamStatusErrorPersistence streamStatusErrorPersistence;

    @Transactional(REQUIRES_NEW)
    public void markStreamAsErrored(final StreamError streamError) {

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection()) {
            if (streamErrorPersistence.save(streamError, connection)) {
                final StreamErrorDetails streamErrorDetails = streamError.streamErrorDetails();
                final UUID streamId = streamErrorDetails.streamId();
                final UUID streamErrorId = streamErrorDetails.id();
                final Long positionInStream = streamErrorDetails.positionInStream();
                final String componentName = streamErrorDetails.componentName();
                final String source = streamErrorDetails.source();

                streamStatusErrorPersistence.markStreamAsErrored(
                        streamId,
                        streamErrorId,
                        positionInStream,
                        componentName,
                        source,
                        connection);
            }
        } catch (final SQLException e) {
            throw new StreamErrorHandlingException("Failed to get connection to view-store", e);
        }
    }

    @Transactional(REQUIRED)
    public void markStreamAsFixed(final UUID streamId, final String source, final String componentName) {

        try (final Connection connection = viewStoreJdbcDataSourceProvider.getDataSource().getConnection()) {
            streamStatusErrorPersistence.unmarkStreamStatusAsErrored(streamId, source, componentName, connection);
            streamErrorPersistence.removeErrorForStream(streamId, source, componentName, connection);
        } catch (final SQLException e) {
            throw new StreamErrorHandlingException("Failed to get connection to view-store", e);
        }
    }
}
