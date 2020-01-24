package uk.gov.justice.services.eventstore.management.validation.process;

import static java.lang.String.format;

import uk.gov.justice.schema.catalog.Catalog;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.everit.json.schema.Schema;

public class SchemaProvider {

    private Map<String, Schema> schemaCache = new HashMap<>();

    @Inject
    private SchemaIdFinder schemaIdFinder;

    @Inject
    private Catalog catalog;

    public Schema getForEvent(final String eventName) {
        return schemaCache.computeIfAbsent(eventName, this::getSchema);
    }

    private Schema getSchema(final String eventName) {

        final String schemaId = schemaIdFinder
                .lookupSchemaIdFor(eventName)
                .orElseThrow(() -> new MissingSchemaIdException(format("No schema id found for event '%s'", eventName)));

        return catalog
                .getSchema(schemaId)
                .orElseThrow(() -> new MissingSchemaException(format("No schema found with schema id '%s', for event '%s'", schemaId, eventName)));
    }
}
