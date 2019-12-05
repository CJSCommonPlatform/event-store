package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static java.lang.Thread.currentThread;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.ConsumeEventQueueTaskManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.EventQueueConsumer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

/**
 * A concurrent implementation of EventStreamConsumerManager and EventStreamConsumerListener.
 *
 * This uses the ManagedExecutorService for concurrency and Queues events according to the Stream
 * Id.
 *
 * When the add method is called
 */
public class ConcurrentEventStreamConsumerManager implements EventStreamConsumerManager, EventStreamConsumptionResolver {

    private static final Object EXCLUSIVE_LOCK = new Object();

    private final ConcurrentHashMap<UUID, Queue<PublishedEvent>> allEventStreams = new ConcurrentHashMap<>();

    @Inject
    private EventsInProcessCounterProvider eventsInProcessCounterProvider;

    @Inject
    private EventStreamsInProgressList eventStreamsInProgressList;

    @Inject
    private ConsumeEventQueueTaskManager consumeEventQueueTaskManager;

    @Inject
    private EventQueueConsumerFactory eventQueueConsumerFactory;

    /**
     * A ConcurrentLinkedQueue is created for each Stream Id and added to a ConcurrentHashMap.  An
     * event is added to the Queue for a Stream Id.
     *
     * If the Queue is not currently being processed a new ConsumeEventQueueTask is created and
     * submitted to the ManagedExecutorService.  The Queue is then added to the
     * eventStreamsInProgress Queue.
     *
     * If the Queue is currently being processed no further action is taken, as the event will be
     * processed by the current ConsumeEventQueueTask.
     *
     * @param publishedEvent - the JsonEnvelope to be consumed
     * @return The number of events added to the stream. Note this is always one and is used to
     * count the number of events consumed
     */
    @Override
    public int add(
            final PublishedEvent publishedEvent,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final UUID commandId) {

        final UUID streamId = publishedEvent.getStreamId();

        final Queue<PublishedEvent> events = allEventStreams.computeIfAbsent(streamId, id -> new ConcurrentLinkedQueue<>());

        final EventsInProcessCounter eventsInProcessCounter = eventsInProcessCounterProvider.getInstance();

        synchronized (EXCLUSIVE_LOCK) {

            while (eventsInProcessCounter.maxNumberOfEventsInProcess()) {
                try {
                    EXCLUSIVE_LOCK.wait();
                } catch (final InterruptedException e) {
                    currentThread().interrupt();
                    break;
                }
            }

            events.offer(publishedEvent);

            if (notInProgress(events)) {
                createAndSubmitTaskFor(events, subscriptionName, catchupCommand, commandId);
            }

            eventsInProcessCounter.incrementEventsInProcessCount();
        }

        return 1;
    }

    /**
     * When a ConsumeEventQueueTask has finished consuming an event Queue, the event Queue is
     * removed from the eventStreamsInProgress Queue
     *
     * @param finishedProcessingMessage - the message containing the Queue that has been consumed.
     */
    @Override
    public boolean isEventConsumptionComplete(final FinishedProcessingMessage finishedProcessingMessage) {

        synchronized (EXCLUSIVE_LOCK) {
            final Queue<PublishedEvent> finishedProcessingMessageQueue = finishedProcessingMessage.getQueue();

            final boolean finishedProcessingMessageQueueEmpty = finishedProcessingMessageQueue.isEmpty();

            if (finishedProcessingMessageQueueEmpty) {
                eventStreamsInProgressList.remove(finishedProcessingMessageQueue);
            }

            return finishedProcessingMessageQueueEmpty;
        }
    }

    @Override
    public void waitForCompletion() {
        eventStreamsInProgressList.blockUntilEmpty();
    }

    @Override
    public void decrementEventsInProcessCount() {

        final EventsInProcessCounter eventsInProcessCounter = eventsInProcessCounterProvider.getInstance();

        synchronized (EXCLUSIVE_LOCK) {
             eventsInProcessCounter.decrementEventsInProcessCount();
             EXCLUSIVE_LOCK.notify();
         }
    }

    private boolean notInProgress(final Queue<PublishedEvent> eventStream) {
        return !eventStreamsInProgressList.contains(eventStream);
    }

    private void createAndSubmitTaskFor(
            final Queue<PublishedEvent> eventStream,
            final String subscriptionName,
            final CatchupCommand catchupCommand,
            final UUID commandId) {

        eventStreamsInProgressList.add(eventStream);

        final EventQueueConsumer eventQueueConsumer = eventQueueConsumerFactory.create(this);
        consumeEventQueueTaskManager.consume(
                eventStream,
                eventQueueConsumer,
                subscriptionName,
                catchupCommand,
                commandId);
    }
}
