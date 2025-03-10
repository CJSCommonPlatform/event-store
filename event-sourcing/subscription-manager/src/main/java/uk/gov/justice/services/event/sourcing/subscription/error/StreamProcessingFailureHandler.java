package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.lang.String.format;

import uk.gov.justice.services.common.configuration.errors.event.EventErrorHandlingConfiguration;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.eventsourcing.source.api.streams.MissingStreamIdException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import javax.inject.Inject;

public class StreamProcessingFailureHandler {

    @Inject
    private ExceptionDetailsRetriever exceptionDetailsRetriever;

    @Inject
    private StreamErrorConverter streamErrorConverter;

    @Inject
    private StreamErrorRepository streamErrorRepository;

    @Inject
    private EventErrorHandlingConfiguration eventErrorHandlingConfiguration;

    public void onStreamProcessingFailure(final JsonEnvelope jsonEnvelope, final Throwable exception, final String componentName) {

        if(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()) {
            final ExceptionDetails exceptionDetails = exceptionDetailsRetriever.getExceptionDetailsFrom(exception);
            final StreamError streamError = streamErrorConverter.asStreamError(exceptionDetails, jsonEnvelope, componentName);

            streamErrorRepository.markStreamAsErrored(streamError);
        }
    }

    public void onStreamProcessingSucceeded(final JsonEnvelope jsonEnvelope, final String componentName) {

        if(eventErrorHandlingConfiguration.isEventErrorHandlingEnabled()) {
            final Metadata metadata = jsonEnvelope.metadata();
            final String source = metadata.source().orElseThrow(() -> new MissingSourceException(format("No 'source' defined in event with id: '%s' and eventName: '%s'", metadata.id(), metadata.name())));
            final UUID streamId = metadata.streamId().orElseThrow(() -> new MissingStreamIdException(format("No streamId defined in event with id: '%s' and eventName: '%s'", metadata.id(), metadata.name())));
            streamErrorRepository.markStreamAsFixed(streamId, source, componentName);
        }
    }
}
