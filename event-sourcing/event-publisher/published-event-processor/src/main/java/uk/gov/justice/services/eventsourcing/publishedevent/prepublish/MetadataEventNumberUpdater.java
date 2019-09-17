package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.spi.DefaultEnvelopeProvider;

import javax.inject.Inject;

public class MetadataEventNumberUpdater {

    @Inject
    private DefaultEnvelopeProvider defaultEnvelopeProvider;

    public Metadata updateMetadataJson(final Metadata metadata, final long previousSequenceNumber, final long sequenceNumber) {
        return defaultEnvelopeProvider.metadataFrom(metadata)
                .withEventNumber(sequenceNumber)
                .withPreviousEventNumber(previousSequenceNumber)
                .build();
    }
}
