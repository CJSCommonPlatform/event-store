package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.justice.services.messaging.spi.DefaultEnvelopeProvider;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetadataEventNumberUpdaterTest {

    @Mock
    private DefaultEnvelopeProvider defaultEnvelopeProvider;

    @InjectMocks
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Test
    public void shouldAddPreviousAndNextSequenceNumberIntoMetadataJson() throws Exception {

        final String name = "example.first-event";
        final String eventSource = "event source";
        final UUID id = UUID.fromString("07652a0b-3a12-4b93-ad24-a33c7c171f19");
        final UUID streamId = UUID.fromString("031e1bec-8c3d-45fb-b206-c70f33678f58");
        final long previousSequenceNumber = 23;
        final long sequenceNumber = 24;

        final Metadata originalMetadata = metadataBuilder()
                .withId(id)
                .withName(name)
                .withStreamId(streamId)
                .withSource(eventSource)
                .build();

        final MetadataBuilder metadataBuilder = mock(MetadataBuilder.class);
        final Metadata expectedMetadata = mock(Metadata.class);

        when(defaultEnvelopeProvider.metadataFrom(originalMetadata)).thenReturn(metadataBuilder);
        when(metadataBuilder.withEventNumber(sequenceNumber)).thenReturn(metadataBuilder);
        when(metadataBuilder.withPreviousEventNumber(previousSequenceNumber)).thenReturn(metadataBuilder);
        when(metadataBuilder.build()).thenReturn(expectedMetadata);

        assertThat(
                metadataEventNumberUpdater.updateMetadataJson(originalMetadata, previousSequenceNumber, sequenceNumber),
                is(expectedMetadata));

        verify(defaultEnvelopeProvider).metadataFrom(originalMetadata);
        verify(metadataBuilder).withEventNumber(sequenceNumber);
        verify(metadataBuilder).withPreviousEventNumber(previousSequenceNumber);
        verify(metadataBuilder).build();
    }
}
