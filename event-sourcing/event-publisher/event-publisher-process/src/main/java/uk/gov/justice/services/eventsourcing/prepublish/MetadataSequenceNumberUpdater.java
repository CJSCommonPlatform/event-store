package uk.gov.justice.services.eventsourcing.prepublish;

import uk.gov.justice.services.eventsourcing.PublishQueueException;

import java.io.IOException;
import java.io.StringReader;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MetadataSequenceNumberUpdater {

    @Inject
    ObjectMapper objectMapper;

    public String updateMetadataJson(final String metadataJson, final long previousSequenceNumber, final long sequenceNumber) {
        final ObjectNode objectNode = asObjectNode(metadataJson);

        objectNode.put("previousSequenceNumber", previousSequenceNumber);
        objectNode.put("sequenceNumber", sequenceNumber);

        return objectNode.toString();
    }

    private ObjectNode asObjectNode(final String metadataJson) {
        try {
            return (ObjectNode) objectMapper.readTree(new StringReader(metadataJson));
        } catch (final IOException e) {
            throw new PublishQueueException("Failed to parse metadata into a json ObjectNode");
        }
    }
}
