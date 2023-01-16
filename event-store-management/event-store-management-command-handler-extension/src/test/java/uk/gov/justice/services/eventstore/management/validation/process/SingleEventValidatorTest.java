package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.Optional;
import java.util.UUID;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class SingleEventValidatorTest {

    @Mock
    private SchemaProvider schemaProvider;

    @Mock
    private JsonStringConverter jsonStringConverter;

    @Mock
    private Logger logger;

    @InjectMocks
    private SingleEventValidator singleEventValidator;

    @Test
    public void shouldSuccessfullyValidateThePayloadOfPublishedEvent() throws Exception {

        final String eventName = "event.name";
        final UUID eventId = fromString("4007249d-8e5a-49d2-bdbb-1d8a960baac5");
        final String payload = "{the: payload}";

        final JSONObject jsonObject = mock(JSONObject.class);
        final Schema schema = mock(Schema.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(publishedEvent.getName()).thenReturn(eventName);
        when(publishedEvent.getId()).thenReturn(eventId);
        when(publishedEvent.getPayload()).thenReturn(payload);
        when(jsonStringConverter.asJsonObject(payload)).thenReturn(jsonObject);
        when(schemaProvider.getForEvent(eventName)).thenReturn(schema);

        assertThat(singleEventValidator.validate(publishedEvent), is(empty()));

        verify(schema).validate(jsonObject);
    }

    @Test
    public void shouldReturnErrorIfThePayloadOfPublishedEventFailsValidation() throws Exception {

        final ValidationException validationException = mock(ValidationException.class);

        final String eventName = "event.name";
        final UUID eventId = fromString("4007249d-8e5a-49d2-bdbb-1d8a960baac5");
        final String payload = "{the: payload}";

        final JSONObject jsonObject = mock(JSONObject.class);
        final Schema schema = mock(Schema.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(publishedEvent.getName()).thenReturn(eventName);
        when(publishedEvent.getId()).thenReturn(eventId);
        when(publishedEvent.getPayload()).thenReturn(payload);
        when(jsonStringConverter.asJsonObject(payload)).thenReturn(jsonObject);
        when(schemaProvider.getForEvent(eventName)).thenReturn(schema);

        doThrow(validationException).when(schema).validate(jsonObject);

        when(validationException.getAllMessages()).thenReturn(asList("error 1", "error 2"));

        final Optional<ValidationError> validationError = singleEventValidator.validate(publishedEvent);

        if (validationError.isPresent()) {
            assertThat(validationError.get().getEventId(), is(eventId));
            assertThat(validationError.get().getEventName(), is(eventName));
            assertThat(validationError.get().getErrorMessage(), is("Event 'event.name' with id '4007249d-8e5a-49d2-bdbb-1d8a960baac5' failed to validate against schema: [error 1, error 2]"));
        } else {
            fail();
        }
    }
}
