package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class EventsInProcessCounterTest {

    @Test
    public void shouldIncrementAndDecrementEventsInProcessCounterByOne() {

        final EventsInProcessCounter eventsInProcessCounter = new EventsInProcessCounter(1);
        final AtomicInteger eventInProcessCount = ReflectionUtil.getValueOfField(eventsInProcessCounter, "eventInProcessCount", AtomicInteger.class);

        eventsInProcessCounter.incrementEventsInProcessCount();

        assertThat(eventInProcessCount.get(), is(1));

        eventsInProcessCounter.decrementEventsInProcessCount();

        assertThat(eventInProcessCount.get(), is(0));
    }

    @Test
    public void shouldDecrementEventsInProcessCounterByGivenNumber() {

        final EventsInProcessCounter eventsInProcessCounter = new EventsInProcessCounter(3);
        final AtomicInteger eventInProcessCount = ReflectionUtil.getValueOfField(eventsInProcessCounter, "eventInProcessCount", AtomicInteger.class);

        eventsInProcessCounter.incrementEventsInProcessCount();
        eventsInProcessCounter.incrementEventsInProcessCount();
        eventsInProcessCounter.incrementEventsInProcessCount();

        assertThat(eventInProcessCount.get(), is(3));

        eventsInProcessCounter.decrementEventsInProcessCountBy(3);

        assertThat(eventInProcessCount.get(), is(0));
    }

    @Test
    public void shouldReturnTrueIfMaxCountReached() {

        final EventsInProcessCounter eventsInProcessCounter = new EventsInProcessCounter(3);

        eventsInProcessCounter.incrementEventsInProcessCount();
        eventsInProcessCounter.incrementEventsInProcessCount();
        eventsInProcessCounter.incrementEventsInProcessCount();

        assertThat(eventsInProcessCounter.maxNumberOfEventsInProcess(), is(true));
    }
}