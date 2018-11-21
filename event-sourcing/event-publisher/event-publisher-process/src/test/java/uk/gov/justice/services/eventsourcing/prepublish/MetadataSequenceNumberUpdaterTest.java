package uk.gov.justice.services.eventsourcing.prepublish;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.CoreMatchers.is;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MetadataSequenceNumberUpdaterTest {

    @Spy
    @SuppressWarnings("unused")
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @InjectMocks
    private MetadataSequenceNumberUpdater metadataSequenceNumberUpdater;

    @Test
    public void shouldAddPreviousAndNextSequenceNumberIntoMetadataJson() throws Exception {

        final long previousSequenceNumber = 23;
        final long sequenceNumber = 24;

        final String metadataJson = "{\n" +
                "   \"id\":\"07652a0b-3a12-4b93-ad24-a33c7c171f19\",\n" +
                "   \"name\":\"example.first-event\",\n" +
                "   \"stream\":{\n" +
                "      \"id\":\"031e1bec-8c3d-45fb-b206-c70f33678f58\"\n" +
                "   },\n" +
                "   \"source\":\"event source\"\n" +
                "}";


        final String updatedMetadata = metadataSequenceNumberUpdater.updateMetadataJson(
                metadataJson,
                previousSequenceNumber,
                sequenceNumber);

        with(updatedMetadata)
                .assertThat("$.id", is("07652a0b-3a12-4b93-ad24-a33c7c171f19"))
                .assertThat("$.name", is("example.first-event"))
                .assertThat("$.stream.id", is("031e1bec-8c3d-45fb-b206-c70f33678f58"))
                .assertThat("$.source", is("event source"))
                .assertThat("$.previousSequenceNumber", is((int) previousSequenceNumber))
                .assertThat("$.sequenceNumber", is((int) sequenceNumber))
        ;
    }
}
