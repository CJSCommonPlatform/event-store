package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventsInProcessCounterProviderTest {

    @Mock
    private EventQueueProcessingConfig eventQueueProcessingConfig;
    
    @InjectMocks
    private EventsInProcessCounterProvider eventsInProcessCounterProvider;

    @Test
    public void shouldCreateWithCorrectMaxTotalEventsInProcess() throws Exception {

        final int maxTotalEventsInProcess = 982734;

        when(eventQueueProcessingConfig.getMaxTotalEventsInProcess()).thenReturn(maxTotalEventsInProcess);

        final EventsInProcessCounter eventsInProcessCounter = eventsInProcessCounterProvider.getInstance();

        assertThat(getValueOfField(eventsInProcessCounter, "maxTotalEventsInProcess", Integer.class), is(maxTotalEventsInProcess));
    }

    @Test
    public void shouldAlwaysReturnTheSameInstance() throws Exception {

        final EventsInProcessCounter eventsInProcessCounter = eventsInProcessCounterProvider.getInstance();
        
        assertThat(eventsInProcessCounterProvider.getInstance(), is(sameInstance(eventsInProcessCounter)));
        assertThat(eventsInProcessCounterProvider.getInstance(), is(sameInstance(eventsInProcessCounter)));
        assertThat(eventsInProcessCounterProvider.getInstance(), is(sameInstance(eventsInProcessCounter)));
        assertThat(eventsInProcessCounterProvider.getInstance(), is(sameInstance(eventsInProcessCounter)));
        assertThat(eventsInProcessCounterProvider.getInstance(), is(sameInstance(eventsInProcessCounter)));
    }
}
