package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class SchemaIdFinder {

    @Inject
    private SchemaIdMappingProvider schemaIdMappingProvider;

    private final Map<String, String> schemaIds = new HashMap<>();

    @PostConstruct
    public void initialize() {

        schemaIds.putAll(schemaIdMappingProvider.mapEventNamesToSchemaIds());
    }

    public Optional<String> lookupSchemaIdFor(final String eventName) {
        return ofNullable(schemaIds.get(eventName));
    }
}
