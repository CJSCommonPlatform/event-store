package uk.gov.justice.services.eventstore.management.publishing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventstore.management.commands.EnablePublishingCommand;
import uk.gov.justice.services.eventstore.management.commands.PublishingCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;


@ExtendWith(MockitoExtension.class)
public class EnablePublishingProcessorTest {

    @Mock
    private PublishingEnabler publishingEnabler;

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private EnablePublishingProcessor enablePublishingProcessor;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldEnableOrDisablePublishingAndFireTheCorrectCommandStateEvents() throws Exception {

        final UUID commandId = randomUUID();
        final PublishingCommand publishingCommand = new EnablePublishingCommand();

        final ZonedDateTime startTime = new UtcClock().now();
        final ZonedDateTime completeTime = startTime.plusSeconds(1);

        when(clock.now()).thenReturn(startTime, completeTime);

        enablePublishingProcessor.enableDisable(publishingCommand, commandId);

        final InOrder inOrder = inOrder(systemCommandStateChangedEventFirer, logger, publishingEnabler);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("Running ENABLE_PUBLISHING");
        inOrder.verify(publishingEnabler).enableOrDisable(publishingCommand);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("ENABLE_PUBLISHING complete");

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent startEvent = allValues.get(0);

        assertThat(startEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(startEvent.getCommandId(), is(commandId));
        assertThat(startEvent.getSystemCommand(), is(publishingCommand));
        assertThat(startEvent.getStatusChangedAt(), is(startTime));
        assertThat(startEvent.getMessage(), is("Running ENABLE_PUBLISHING"));

        final SystemCommandStateChangedEvent completeEvent = allValues.get(1);

        assertThat(completeEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(completeEvent.getCommandId(), is(commandId));
        assertThat(completeEvent.getSystemCommand(), is(publishingCommand));
        assertThat(completeEvent.getStatusChangedAt(), is(completeTime));
        assertThat(completeEvent.getMessage(), is("ENABLE_PUBLISHING complete"));
    }
}
