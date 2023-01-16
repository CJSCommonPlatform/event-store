package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.time.ZoneOffset.UTC;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidStreamIdException;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelopeProvider;
import uk.gov.justice.services.test.utils.common.helper.StoppedClock;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventConverterTest {

    private final static String PAYLOAD_FIELD_NAME = "field";
    private final static String PAYLOAD_FIELD_VALUE = "Value";

    private final static UUID ID = UUID.randomUUID();
    private final static UUID STREAM_ID = UUID.randomUUID();
    private final static Long SEQUENCE_ID = 5L;
    private final static String NAME = "test.event.did-something";
    private final static String METADATA_JSON = "{\"id\": \"" + ID.toString() + "\", " +
            "\"name\": \"" + NAME + "\"" +
            "}";
    private final static String PAYLOAD_JSON = "{\"" + PAYLOAD_FIELD_NAME + "\":\"" + PAYLOAD_FIELD_VALUE + "\"}";
    private final Clock clock = new StoppedClock(new UtcClock().now());
    private EventConverter eventConverter;


    @Before
    public void setup() {
        eventConverter = new EventConverter();
        eventConverter.stringToJsonObjectConverter = new StringToJsonObjectConverter();
        eventConverter.jsonObjectEnvelopeConverter = new DefaultJsonObjectEnvelopeConverter();
        eventConverter.defaultJsonEnvelopeProvider = new DefaultJsonEnvelopeProvider();
    }

    @Test
    public void shouldCreateEventLog() throws Exception {
        JsonEnvelope envelope = envelopeFrom(
                metadataBuilder()
                        .withId(ID)
                        .withName(NAME)
                        .withStreamId(STREAM_ID)
                        .withVersion(SEQUENCE_ID)
                        .createdAt(clock.now()),
                createObjectBuilder().add(PAYLOAD_FIELD_NAME, PAYLOAD_FIELD_VALUE));
        Event event = eventConverter.eventOf(envelope);

        assertThat(event.getId(), equalTo(ID));
        assertThat(event.getName(), equalTo(NAME));
        assertThat(event.getStreamId(), equalTo(STREAM_ID));
        assertThat(event.getPositionInStream(), equalTo(SEQUENCE_ID));
        assertThat(ZonedDateTimes.toString(event.getCreatedAt()), is(ZonedDateTimes.toString(clock.now())));
        assertEquals(METADATA_JSON, event.getMetadata(), false);
        assertEquals(envelope.payloadAsJsonObject().toString(), event.getPayload(), false);
    }

    @Test(expected = InvalidStreamIdException.class)
    public void shouldThrowExceptionOnNullStreamId() throws Exception {
        eventConverter.eventOf(envelopeFrom(metadataBuilder().withId(ID).withName(NAME), createObjectBuilder()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMissingCreatedAt() throws Exception {
        eventConverter.eventOf((envelopeFrom(metadataBuilder().withId(ID).withName(NAME).withStreamId(STREAM_ID), createObjectBuilder())));
    }

    @Test
    public void shouldCreateEnvelope() throws Exception {
        final JsonEnvelope actualEnvelope = eventConverter.envelopeOf(new Event(ID, STREAM_ID, SEQUENCE_ID, NAME, METADATA_JSON, PAYLOAD_JSON, new UtcClock().now()));

        assertThat(actualEnvelope.metadata().id(), equalTo(ID));
        assertThat(actualEnvelope.metadata().name(), equalTo(NAME));
        String actualPayload = actualEnvelope.payloadAsJsonObject().toString();
        assertEquals(PAYLOAD_JSON, actualPayload, false);
    }

    @Test
    public void shouldConvertToAndFromJsonEnvelope() throws Exception {

        final ZonedDateTime createdAt = ZonedDateTime.of(2020, 11, 28, 17, 30, 0, 0, UTC);

        final Metadata metadata = metadataBuilder()
                .createdAt(createdAt)
                .withStreamId(STREAM_ID)
                .withId(ID)
                .withName(NAME)
                .withVersion(SEQUENCE_ID)
                .build();

        final String metadataJson = metadata.asJsonObject().toString();

        final Event event = new Event(ID, STREAM_ID, SEQUENCE_ID, NAME, metadataJson, PAYLOAD_JSON, createdAt);

        final JsonEnvelope jsonEnvelope = eventConverter.envelopeOf(event);

        final Event convertedEvent = eventConverter.eventOf(jsonEnvelope);

        assertThat(convertedEvent, is(event));
    }
}
