package uk.gov.justice.services.eventsourcing.prepublish;

import static uk.gov.justice.services.messaging.Envelope.metadataFrom;

import uk.gov.justice.services.messaging.Metadata;

public class MetadataEventNumberUpdater {

    public Metadata updateMetadataJson(final Metadata metadata, final long previousSequenceNumber, final long sequenceNumber) {
        return metadataFrom(metadata)
                .withEventNumber(sequenceNumber)
                .withPreviousEventNumber(previousSequenceNumber)
                .build();
    }
}
