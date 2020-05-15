package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.schema.catalog.Catalog;

import org.everit.json.schema.Schema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaProviderTest {

    @Mock
    private SchemaIdFinder schemaIdFinder;

    @Mock
    private Catalog catalog;

    @InjectMocks
    private SchemaProvider schemaProvider;

    @Test
    public void shouldGetTheCorrectSchemaForTheEventName() throws Exception {

        final String eventName = "eventName";
        final String schemeId = "schema id";
        final Schema schema = mock(Schema.class);

        when(schemaIdFinder.lookupSchemaIdFor(eventName)).thenReturn(of(schemeId));
        when(catalog.getSchema(schemeId)).thenReturn(of(schema));

        assertThat(schemaProvider.getForEvent(eventName), is(schema));
    }

    @Test
    public void shouldCacheResultsOfSchemaLookup() throws Exception {

        final String eventName = "eventName";
        final String schemaId = "schema id";
        final Schema schema = mock(Schema.class);

        when(schemaIdFinder.lookupSchemaIdFor(eventName)).thenReturn(of(schemaId));
        when(catalog.getSchema(schemaId)).thenReturn(of(schema));

        assertThat(schemaProvider.getForEvent(eventName), is(schema));
        assertThat(schemaProvider.getForEvent(eventName), is(schema));
        assertThat(schemaProvider.getForEvent(eventName), is(schema));
        assertThat(schemaProvider.getForEvent(eventName), is(schema));
        assertThat(schemaProvider.getForEvent(eventName), is(schema));
        assertThat(schemaProvider.getForEvent(eventName), is(schema));
        assertThat(schemaProvider.getForEvent(eventName), is(schema));

        verify(schemaIdFinder, times(1)).lookupSchemaIdFor(eventName);
        verify(catalog, times(1)).getSchema(schemaId);
    }

    @Test
    public void shouldThrowExceptionIfNoSchemaIdFoundForEventName() throws Exception {

        final String eventName = "eventName";

        when(schemaIdFinder.lookupSchemaIdFor(eventName)).thenReturn(empty());

        try {
            schemaProvider.getForEvent(eventName);
            fail();
        } catch (final MissingSchemaIdException expected) {
            assertThat(expected.getMessage(), is("No schema id found for event 'eventName'"));
        }
    }

    @Test
    public void shouldThrowExceptionIfNoSchemaFoundForSchemaId() throws Exception {

        final String eventName = "eventName";
        final String schemaId = "schema id";

        when(schemaIdFinder.lookupSchemaIdFor(eventName)).thenReturn(of(schemaId));
        when(catalog.getSchema(schemaId)).thenReturn(empty());

        try {
            schemaProvider.getForEvent(eventName);
            fail();
        } catch (final MissingSchemaException expected) {
            assertThat(expected.getMessage(), is("No schema found with schema id 'schema id', for event 'eventName'"));
        }
    }
}
