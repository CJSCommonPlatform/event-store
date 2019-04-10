package uk.gov.justice.services.event.sourcing.subscription.startup.manager;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.event.sourcing.subscription.catchup.EventCatchupException;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.FinishedProcessingMessage;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.ConsumeEventQueueBean;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.EventQueueConsumer;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConcurrentEventStreamConsumerManagerTest {

    @Mock
    private ConsumeEventQueueBean consumeEventQueueBean;

    @Mock
    private EventQueueConsumerFactory eventQueueConsumerFactory;

    @Spy
    private EventStreamsInProgressList eventStreamsInProgressList = new EventStreamsInProgressList();

    @InjectMocks
    private ConcurrentEventStreamConsumerManager concurrentEventStreamConsumerManager;

    @Captor
    private ArgumentCaptor<Queue<JsonEnvelope>> eventQueueCaptor;

    @Test
    public void shouldCreateQueueAndCreateTaskToConsumeQueueForNewStreamId() {

        final String subscriptionName = "subscriptionName";
        final JsonEnvelope event = generateJsonEnvelope(randomUUID(), 1);

        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);

        when(eventQueueConsumerFactory.create(concurrentEventStreamConsumerManager)).thenReturn(eventQueueConsumer);

        concurrentEventStreamConsumerManager.add(event, subscriptionName);

        verify(consumeEventQueueBean).consume(eventQueueCaptor.capture(), eq(eventQueueConsumer), eq(subscriptionName));

        final Queue<JsonEnvelope> events = eventQueueCaptor.getValue();
        assertThat(events.size(), is(1));
        assertThat(events.poll(), is(event));

    }

    @Test
    public void shouldNotCreateQueueOrCreateTaskIfEventIsSameStreamId() {

        final String subscriptionName = "subscriptionName";
        final UUID streamId = randomUUID();
        final JsonEnvelope event_1 = generateJsonEnvelope(streamId, 1);
        final JsonEnvelope event_2 = generateJsonEnvelope(streamId, 2);
        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);

        when(eventQueueConsumerFactory.create(concurrentEventStreamConsumerManager)).thenReturn(eventQueueConsumer);

        concurrentEventStreamConsumerManager.add(event_1, subscriptionName);
        concurrentEventStreamConsumerManager.add(event_2, subscriptionName);

        verify(consumeEventQueueBean).consume(eventQueueCaptor.capture(), eq(eventQueueConsumer), eq(subscriptionName));

        final Queue<JsonEnvelope> eventsStream = eventQueueCaptor.getValue();
        assertThat(eventsStream.size(), is(2));
        assertThat(eventsStream.poll(), is(event_1));
        assertThat(eventsStream.poll(), is(event_2));
    }

    @Test
    public void shouldCreateQueueForEachStreamId() {

        final String subscriptionName = "subscriptionName";
        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final JsonEnvelope event_1 = generateJsonEnvelope(streamId_1, 1);
        final JsonEnvelope event_2 = generateJsonEnvelope(streamId_2, 1);
        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);

        when(eventQueueConsumerFactory.create(concurrentEventStreamConsumerManager)).thenReturn(eventQueueConsumer);

        concurrentEventStreamConsumerManager.add(event_1, subscriptionName);
        concurrentEventStreamConsumerManager.add(event_2, subscriptionName);

        verify(consumeEventQueueBean, times(2)).consume(eventQueueCaptor.capture(), eq(eventQueueConsumer), eq(subscriptionName));

        final List<Queue<JsonEnvelope>> allValues = eventQueueCaptor.getAllValues();

        final Queue<JsonEnvelope> eventsStream_1 = allValues.get(0);
        assertThat(eventsStream_1.size(), is(1));
        assertThat(eventsStream_1.poll(), is(event_1));

        final Queue<JsonEnvelope> eventsStream_2 = allValues.get(1);
        assertThat(eventsStream_2.size(), is(1));
        assertThat(eventsStream_2.poll(), is(event_2));
    }

    @Test
    public void shouldBeAbleToFinishQueueAndAllowAnotherProcessToPickupQueueIfNotEmpty() {

        final String subscriptionName = "subscriptionName";
        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final JsonEnvelope event_1 = generateJsonEnvelope(streamId_1, 1);
        final JsonEnvelope event_2 = generateJsonEnvelope(streamId_2, 1);
        final EventQueueConsumer eventQueueConsumer = mock(EventQueueConsumer.class);

        when(eventQueueConsumerFactory.create(concurrentEventStreamConsumerManager)).thenReturn(eventQueueConsumer);

        concurrentEventStreamConsumerManager.add(event_1, subscriptionName);

        verify(consumeEventQueueBean).consume(eventQueueCaptor.capture(), eq(eventQueueConsumer), eq(subscriptionName));

        final Queue<JsonEnvelope> eventsStream_1 = eventQueueCaptor.getValue();
        assertThat(eventsStream_1.size(), is(1));
        assertThat(eventsStream_1.poll(), is(event_1));

        concurrentEventStreamConsumerManager.isEventConsumptionComplete(new FinishedProcessingMessage(eventsStream_1));
        concurrentEventStreamConsumerManager.add(event_2, subscriptionName);

        verify(consumeEventQueueBean, times(2)).consume(eventQueueCaptor.capture(), eq(eventQueueConsumer), eq(subscriptionName));

        final Queue<JsonEnvelope> eventsStream_2 = eventQueueCaptor.getValue();
        assertThat(eventsStream_2.size(), is(1));
        assertThat(eventsStream_2.poll(), is(event_2));
    }

    @Test
    public void shouldThrowExceptionIfStreamIdIsMissingFromEvent() {

        final String subscriptionName = "subscriptionName";
        final JsonEnvelope event = envelopeFrom(metadataBuilder()
                        .withId(UUID.fromString("caeb2531-02f7-49d4-97da-11a692801035"))
                        .withName("testEvent")
                        .withPosition(1)
                        .withSource("test_source"),
                createObjectBuilder().build());

        try {
            concurrentEventStreamConsumerManager.add(event, subscriptionName);
            fail("Expected EventCatchupException to be thrown");
        } catch (final EventCatchupException e) {
            assertThat(e.getMessage(), is("Event with id 'caeb2531-02f7-49d4-97da-11a692801035' has no streamId"));
        }
    }

    @Test
    public void shouldBlockOnTheEventsStreamInProgressListWhenWaitingForCompletion() throws Exception {

        concurrentEventStreamConsumerManager.waitForCompletion();

        verify(eventStreamsInProgressList).blockUntilEmpty();
    }

    private JsonEnvelope generateJsonEnvelope(final UUID streamId, final int version) {
        return envelopeFrom(metadataBuilder()
                        .withId(randomUUID())
                        .withName("testEvent")
                        .withStreamId(streamId)
                        .withPosition(version)
                        .withSource("test_source"),
                createObjectBuilder().build());
    }
}
