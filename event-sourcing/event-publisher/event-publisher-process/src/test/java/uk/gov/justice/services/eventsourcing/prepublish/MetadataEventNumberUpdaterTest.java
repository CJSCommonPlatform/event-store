package uk.gov.justice.services.eventsourcing.prepublish;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MetadataEventNumberUpdaterTest {

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

        final Metadata updatedMetadata = metadataEventNumberUpdater.updateMetadataJson(
                originalMetadata,
                previousSequenceNumber,
                sequenceNumber);

        assertThat(updatedMetadata.id(), is(id));
        assertThat(updatedMetadata.name(), is(name));
        assertThat(updatedMetadata.streamId(), is(of(streamId)));
        assertThat(updatedMetadata.source(), is(of(eventSource)));
        assertThat(updatedMetadata.eventNumber(), is(of(sequenceNumber)));
        assertThat(updatedMetadata.previousEventNumber(), is(of(previousSequenceNumber)));
    }
}
