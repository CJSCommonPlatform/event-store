package uk.gov.justice.services.event.buffer.core.service;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.event.buffer.core.repository.streambuffer.EventBufferEvent;
import uk.gov.justice.services.event.buffer.core.repository.streambuffer.EventBufferJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.StreamStatusJdbcRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.Subscription;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.test.utils.common.stream.StreamCloseSpy;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ConsecutiveEventBufferServiceTest {

    @Mock
    @SuppressWarnings("unused")
    private Logger logger;

    @Mock
    private StreamStatusJdbcRepository streamStatusJdbcRepository;

    @Mock
    private EventBufferJdbcRepository streamBufferRepository;

    @Mock
    private JsonObjectEnvelopeConverter jsonObjectEnvelopeConverter;

    @InjectMocks
    private ConsecutiveEventBufferService bufferService;


    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfNoStreamId() {

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("my-event").withPosition(1L),
                createObjectBuilder()
        );

        bufferService.currentOrderedEventsWith(event);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowZeroVersion() {

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("my-event").withPosition(0L),
                createObjectBuilder()
        );

        bufferService.currentOrderedEventsWith(event);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowNullVersion() {

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("my-event"),
                createObjectBuilder()
        );
        bufferService.currentOrderedEventsWith(event);
    }

    @Test
    public void shouldIgnoreObsoleteEvent() {

        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";

        final Subscription subscription = mock(Subscription.class);

        when(streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(subscription));
        when(subscription.getPosition()).thenReturn(4L);

        final JsonEnvelope event_3 = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName).withStreamId(streamId).withPosition(3L),
                createObjectBuilder()
        );

        assertThat(bufferService.currentOrderedEventsWith(event_3), is(empty()));

        final JsonEnvelope event_4 = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName).withStreamId(streamId).withPosition(4L),
                createObjectBuilder()
        );

        assertThat(bufferService.currentOrderedEventsWith(event_4), is(empty()));

        verifyZeroInteractions(streamBufferRepository);

        final InOrder inOrder = inOrder(streamStatusJdbcRepository);
        
        inOrder.verify(streamStatusJdbcRepository).updateSource(streamId, source);
        inOrder.verify(streamStatusJdbcRepository).insertOrDoNothing(new Subscription(streamId, 0L, source));
        inOrder.verify(streamStatusJdbcRepository).findByStreamIdAndSource(streamId, source);

    }

    @Test
    public void shouldReturnEventThatIsInCorrectOrder() {
        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";

        final Subscription subscription = mock(Subscription.class);

        when(streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(subscription));
        when(subscription.getPosition()).thenReturn(4L);

        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(Stream.empty());

        final JsonEnvelope incomingEvent = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName).withStreamId(streamId).withPosition(5L),
                createObjectBuilder()
        );

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        assertThat(returnedEvents, contains(incomingEvent));

        final InOrder inOrder = inOrder(streamStatusJdbcRepository);

        inOrder.verify(streamStatusJdbcRepository).updateSource(streamId, source);
        inOrder.verify(streamStatusJdbcRepository).insertOrDoNothing(new Subscription(streamId, 0L, source));
        inOrder.verify(streamStatusJdbcRepository).findByStreamIdAndSource(streamId, source);

    }

    @Test
    public void shouldIncrementVersionOnIncomingEventInCorrectOrder() {
        final UUID streamId = UUID.fromString("8d104aa3-6ea5-4569-8730-4231e5faaaaa");

        final String source = "source";
        final JsonEnvelope incomingEvent = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("source.event-name").withSource(source).withStreamId(streamId).withPosition(5L),
                createObjectBuilder()
        );

        final Subscription subscription = mock(Subscription.class);

        when(streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(subscription));
        when(subscription.getPosition()).thenReturn(4L);
        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(Stream.empty());

        bufferService.currentOrderedEventsWith(incomingEvent);

        final InOrder inOrder = inOrder(streamStatusJdbcRepository);

        inOrder.verify(streamStatusJdbcRepository).updateSource(streamId, source);
        inOrder.verify(streamStatusJdbcRepository).insertOrDoNothing(new Subscription(streamId, 0L, source));
        inOrder.verify(streamStatusJdbcRepository).findByStreamIdAndSource(streamId, source);
        inOrder.verify(streamStatusJdbcRepository).update(new Subscription(streamId, 5L, source));

    }

    @Test
    public void shouldStoreEventIncomingNotInOrderAndReturnEmpty() {
        final String eventName = "source.events.something.happened";
        final String source = "source";
        final UUID streamId = randomUUID();

        final JsonEnvelope incomingEvent = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName).withStreamId(streamId).withPosition(6L),
                createObjectBuilder()
        );

        final Subscription subscription = mock(Subscription.class);

        when(streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(subscription));
        when(subscription.getPosition()).thenReturn(4L);
        when(streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(new Subscription(streamId, 4L, source)));

        when(jsonObjectEnvelopeConverter.asJsonString(incomingEvent)).thenReturn("someStringRepresentation");

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);

        final InOrder inOrder = inOrder(streamStatusJdbcRepository, streamBufferRepository);

        inOrder.verify(streamStatusJdbcRepository).updateSource(streamId, source);
        inOrder.verify(streamStatusJdbcRepository).insertOrDoNothing(new Subscription(streamId, 0L, source));
        inOrder.verify(streamStatusJdbcRepository).findByStreamIdAndSource(streamId, source);
        inOrder.verify(streamBufferRepository).insert(new EventBufferEvent(streamId, 6L, "someStringRepresentation", source));

        assertThat(returnedEvents, empty());
    }

    @Test
    public void shouldReturnConsecutiveBufferedEventsIfIncomingEventFillsTheVersionGap() {

        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";

        final Subscription subscription = mock(Subscription.class);

        when(streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(subscription));
        when(subscription.getPosition()).thenReturn(2L);

        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(
                Stream.of(new EventBufferEvent(streamId, 4L, "someEventContent4", "source_4"),
                        new EventBufferEvent(streamId, 5L, "someEventContent5", "source_5"),
                        new EventBufferEvent(streamId, 6L, "someEventContent6", "source_6"),
                        new EventBufferEvent(streamId, 8L, "someEventContent8", "source_8"),
                        new EventBufferEvent(streamId, 9L, "someEventContent9", "source_9"),
                        new EventBufferEvent(streamId, 10L, "someEventContent10", "source_10"),
                        new EventBufferEvent(streamId, 11L, "someEventContent11", "source_11")));

        final JsonEnvelope bufferedEvent4 = mock(JsonEnvelope.class);
        final JsonEnvelope bufferedEvent5 = mock(JsonEnvelope.class);
        final JsonEnvelope bufferedEvent6 = mock(JsonEnvelope.class);

        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent4")).thenReturn(bufferedEvent4);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent5")).thenReturn(bufferedEvent5);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent6")).thenReturn(bufferedEvent6);

        final JsonEnvelope incomingEvent = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName).withStreamId(streamId).withPosition(3L),
                createObjectBuilder()
        );

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        assertThat(returnedEvents, contains(incomingEvent, bufferedEvent4, bufferedEvent5, bufferedEvent6));

        final InOrder inOrder = inOrder(streamStatusJdbcRepository);

        inOrder.verify(streamStatusJdbcRepository).updateSource(streamId, source);
        inOrder.verify(streamStatusJdbcRepository).insertOrDoNothing(new Subscription(streamId, 0L, source));
        inOrder.verify(streamStatusJdbcRepository).findByStreamIdAndSource(streamId, source);

    }

    @Test
    public void shoulCloseSourceStreamOnConsecutiveStreamClose() {

        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";

        final Subscription subscription = mock(Subscription.class);

        when(streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(subscription));
        when(subscription.getPosition()).thenReturn(2L);

        final StreamCloseSpy sourceStreamSpy = new StreamCloseSpy();

        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(
                Stream.of(new EventBufferEvent(streamId, 4L, "someEventContent4", source),
                        new EventBufferEvent(streamId, 8L, "someEventContent8", source)).onClose(sourceStreamSpy)
        );

        final JsonEnvelope bufferedEvent4 = mock(JsonEnvelope.class);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent4")).thenReturn(bufferedEvent4);

        final JsonEnvelope incomingEvent = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName).withStreamId(streamId).withPosition(3L),
                createObjectBuilder()
        );

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);
        returnedEvents.close();

        assertThat(sourceStreamSpy.streamClosed(), is(true));

        final InOrder inOrder = inOrder(streamStatusJdbcRepository);

        inOrder.verify(streamStatusJdbcRepository).updateSource(streamId, source);
        inOrder.verify(streamStatusJdbcRepository).insertOrDoNothing(new Subscription(streamId, 0L, source));
        inOrder.verify(streamStatusJdbcRepository).findByStreamIdAndSource(streamId, source);

    }

    @Test
    public void shouldRemoveEventsFromBufferOnceStreamed() {

        final UUID streamId = randomUUID();
        final String source = "source";
        final String eventName = "source.event.name";
        final Subscription subscription = mock(Subscription.class);

        when(streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source)).thenReturn(of(subscription));
        when(subscription.getPosition()).thenReturn(2L);


        final EventBufferEvent event4 = new EventBufferEvent(streamId, 4L, "someEventContent4", "source_1");
        final EventBufferEvent event5 = new EventBufferEvent(streamId, 5L, "someEventContent5", "source_2");
        final EventBufferEvent event6 = new EventBufferEvent(streamId, 6L, "someEventContent6", "source_3");

        when(streamBufferRepository.findStreamByIdAndSource(streamId, source)).thenReturn(
                Stream.of(event4, event5, event6));

        final JsonEnvelope bufferedEvent4 = mock(JsonEnvelope.class);
        final JsonEnvelope bufferedEvent5 = mock(JsonEnvelope.class);
        final JsonEnvelope bufferedEvent6 = mock(JsonEnvelope.class);

        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent4")).thenReturn(bufferedEvent4);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent5")).thenReturn(bufferedEvent5);
        when(jsonObjectEnvelopeConverter.asEnvelope("someEventContent6")).thenReturn(bufferedEvent6);

        final JsonEnvelope incomingEvent = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName).withStreamId(streamId).withPosition(3L),
                createObjectBuilder()
        );

        final Stream<JsonEnvelope> returnedEvents = bufferService.currentOrderedEventsWith(incomingEvent);

        assertThat(returnedEvents, contains(incomingEvent, bufferedEvent4, bufferedEvent5, bufferedEvent6));

        final InOrder inOrder = inOrder(streamStatusJdbcRepository);

        inOrder.verify(streamStatusJdbcRepository).updateSource(streamId, source);
        inOrder.verify(streamStatusJdbcRepository).insertOrDoNothing(new Subscription(streamId, 0L, source));
        inOrder.verify(streamStatusJdbcRepository).findByStreamIdAndSource(streamId, source);

        verify(streamBufferRepository).remove(event4);
        verify(streamBufferRepository).remove(event5);
        verify(streamBufferRepository).remove(event6);
    }
}
