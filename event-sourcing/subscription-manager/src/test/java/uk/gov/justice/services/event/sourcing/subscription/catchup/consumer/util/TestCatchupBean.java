package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.util;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventQueueConsumerFactory;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventStreamsInProgressList;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.ConsumeEventQueueBean;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

@Singleton
public class TestCatchupBean {

    @Resource
    ManagedExecutorService managedExecutorService;

    @Inject
    DummyTransactionalEventProcessor transactionalEventProcessor;

    @Inject
    EventStreamsInProgressList eventStreamsInProgressList;

    @Inject
    private ConsumeEventQueueBean consumeEventQueueBean;

    @Inject
    private EventQueueConsumerFactory eventQueueConsumerFactory;

    @Inject
    Logger logger;

    public void run(final StopWatch stopWatch) {
        logger.info("running!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        final int numberOfStreams = 10;
        final int numberOfEventsToCreate = 60;
        final int numberOfUniqueEventNames = 10;

        transactionalEventProcessor.setExpectedNumberOfEvents(numberOfEventsToCreate);

        final EventFactory eventFactory = new EventFactory(numberOfStreams, numberOfUniqueEventNames);
        final Stream<JsonEnvelope> eventStream = eventFactory.generateEvents(numberOfEventsToCreate).stream();

        final EventStreamConsumerManager eventStreamConsumerManager = new ConcurrentEventStreamConsumerManager(
                eventStreamsInProgressList,
                consumeEventQueueBean,
                eventQueueConsumerFactory);

        stopWatch.start();
        final int totalEventsProcessed = eventStream.mapToInt(event -> {
            eventStreamConsumerManager.add(event, "");
            return 1;
        }).sum();

        eventStreamConsumerManager.waitForCompletion();

        logger.info("Total events processed: " + totalEventsProcessed);
    }
}
