package uk.gov.justice.services.event.sourcing.subscription.startup.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.junit.Test;

public class EventStreamsInProgressListTest {

    private final EventStreamsInProgressList eventStreamsInProgressList = new EventStreamsInProgressList();

    @Test
    public void shouldBlockUntilEmpty() throws Exception {

        final int queueCount = 10_000;

        final Set<Queue<JsonEnvelope>> allStreams = new HashSet<>();

        for (int i = 0; i < queueCount; i++) {
            final Queue<JsonEnvelope> eventStream = mock(Queue.class);

            eventStreamsInProgressList.add(eventStream);
            allStreams.add(eventStream);
        }

        new Thread(() -> {
            for(final Queue<JsonEnvelope> eventStream: allStreams) {
                eventStreamsInProgressList.remove(eventStream);
            }
        }).start();

        assertThat(eventStreamsInProgressList.isEmpty(), is(false));

        eventStreamsInProgressList.blockUntilEmpty();

        assertThat(eventStreamsInProgressList.isEmpty(), is(true));
    }
}