package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaIdFinderTest {

    @Mock
    private SchemaIdMappingProvider schemaIdMappingProvider;

    @InjectMocks
    private SchemaIdFinder schemaIdFinder;

    @Test
    public void shouldFindTheCorrectSchemaIdForEventName() throws Exception {

        final ImmutableMap<String, String> schemaIds = ImmutableMap.of("eventName_1", "schemaId_1", "eventName_2", "schemaId_2");

        when(schemaIdMappingProvider.mapEventNamesToSchemaIds()).thenReturn(schemaIds);

        schemaIdFinder.initialize();

        assertThat(schemaIdFinder.lookupSchemaIdFor("eventName_1"), is(of("schemaId_1")));
        assertThat(schemaIdFinder.lookupSchemaIdFor("eventName_2"), is(of("schemaId_2")));
        assertThat(schemaIdFinder.lookupSchemaIdFor("eventName_3"), is(empty()));
    }
}
