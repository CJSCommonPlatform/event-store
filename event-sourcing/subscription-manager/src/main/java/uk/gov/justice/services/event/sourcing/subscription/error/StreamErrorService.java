package uk.gov.justice.services.event.sourcing.subscription.error;

import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorDetails;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.StreamStatusJdbcRepository;

import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class StreamErrorService {

    @Inject
    private StreamErrorRepository streamErrorRepository;

    @Inject
    private StreamStatusJdbcRepository streamStatusJdbcRepository;

    @Transactional(REQUIRES_NEW)
    public void markStreamAsErrored(final StreamError streamError) {

        final StreamErrorDetails streamErrorDetails = streamError.streamErrorDetails();

        final UUID streamId = streamErrorDetails.streamId();
        final UUID streamErrorId = streamErrorDetails.id();
        final Long positionInStream = streamErrorDetails.positionInStream();
        final String componentName = streamErrorDetails.componentName();
        final String source = streamErrorDetails.source();

        streamErrorRepository.save(streamError);
        streamStatusJdbcRepository.markStreamAsErrored(
                streamId,
                streamErrorId,
                positionInStream,
                componentName,
                source);
    }

    @Transactional(REQUIRED)
    public void markStreamAsFixed(final UUID streamId, final String source, final String componentName) {
        streamStatusJdbcRepository.unmarkStreamAsErrored(streamId, source, componentName);
        streamErrorRepository.removeErrorForStream(streamId, source, componentName);
    }
}
