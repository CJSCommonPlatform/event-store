package uk.gov.justice.services.eventstore.management.replay.process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.util.UtcClock;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@RunWith(MockitoJUnitRunner.class)
public class ReplayEventToEventListenerRunnerTest {

    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final UUID COMMAND_RUNTIME_ID = UUID.randomUUID();

    @Mock
    private EventSourceNameFinder eventSourceNameFinder;

    @Mock
    private ReplayEventToEventListenerProcessorBean replayEventToEventListenerProcessorBean;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private ReplayEventToEventListenerRunner replayEventToEventListenerRunner;

    @Test
    public void buildContextWithEventSourceNameAndPassItToProcessorBean() {
        final String listenerEventSourceName = "listenerEventSourceName";
        when(eventSourceNameFinder.getEventSourceNameOfEventListener()).thenReturn(listenerEventSourceName);

        replayEventToEventListenerRunner.run(COMMAND_ID, COMMAND_RUNTIME_ID);

        verify(replayEventToEventListenerProcessorBean).perform(argThat(new ArgumentMatcher<ReplayEventContext>() {
            @Override
            public boolean matches(Object o) {
                final ReplayEventContext actualContext = (ReplayEventContext) o;
                assertThat(actualContext.getCommandId(), is(COMMAND_ID));
                assertThat(actualContext.getCommandRuntimeId(), is(COMMAND_RUNTIME_ID));
                assertThat(actualContext.getEventSourceName(), is(listenerEventSourceName));
                assertThat(actualContext.getComponentName(), is(EVENT_LISTENER));

                return true;
            }
        }));
    }
}