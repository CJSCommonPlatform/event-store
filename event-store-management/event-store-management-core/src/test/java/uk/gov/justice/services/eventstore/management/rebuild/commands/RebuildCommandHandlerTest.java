package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;
import uk.gov.justice.services.eventstore.management.commands.RebuildCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class RebuildCommandHandlerTest {

    @Mock
    private PublishedEventRebuilder publishedEventRebuilder;

    @Mock
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private RebuildCommandHandler rebuildCommandHandler;

    @Captor
    private ArgumentCaptor<SystemCommandStateChangedEvent> systemCommandStateChangedEventCaptor;

    @Test
    public void shouldRunRebuild() throws Exception {

        final UUID commandId = randomUUID();
        final ZonedDateTime rebuildStartedAt = of(2019, 5, 24, 12, 0, 0, 0, UTC);
        final ZonedDateTime rebuildCompletedAt = rebuildStartedAt.plusSeconds(1);

        final RebuildCommand rebuildCommand = new RebuildCommand();

        when(clock.now()).thenReturn(rebuildStartedAt, rebuildCompletedAt);

        rebuildCommandHandler.doRebuild(rebuildCommand, commandId);

        final InOrder inOrder = inOrder(systemCommandStateChangedEventFirer, logger, publishedEventRebuilder);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("REBUILD started at Fri May 24 12:00:00 Z 2019");
        inOrder.verify(publishedEventRebuilder).rebuild();
        inOrder.verify(logger).info("REBUILD command completed at Fri May 24 12:00:01 Z 2019");
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("Rebuild took 1000 milliseconds");

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent startEvent = allValues.get(0);
        final SystemCommandStateChangedEvent endEvent = allValues.get(1);

        assertThat(startEvent.getCommandId(), is(commandId));
        assertThat(startEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(startEvent.getSystemCommand(), is(rebuildCommand));
        assertThat(startEvent.getStatusChangedAt(), is(rebuildStartedAt));
        assertThat(startEvent.getMessage(), is("REBUILD started at Fri May 24 12:00:00 Z 2019"));

        assertThat(endEvent.getCommandId(), is(commandId));
        assertThat(endEvent.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(endEvent.getSystemCommand(), is(rebuildCommand));
        assertThat(endEvent.getStatusChangedAt(), is(rebuildCompletedAt));
        assertThat(endEvent.getMessage(), is("Rebuild took 1000 milliseconds"));
    }

    @Test
    public void shouldFireTheFailedEventIfRebuildFails() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");

        final UUID commandId = randomUUID();
        final ZonedDateTime rebuildStartedAt = of(2019, 5, 24, 12, 0, 0, 0, UTC);
        final ZonedDateTime rebuildCompletedAt = rebuildStartedAt.plusSeconds(1);

        final RebuildCommand rebuildCommand = new RebuildCommand();

        when(clock.now()).thenReturn(rebuildStartedAt, rebuildCompletedAt);
        doThrow(nullPointerException).when(publishedEventRebuilder).rebuild();

        rebuildCommandHandler.doRebuild(rebuildCommand, commandId);

        final InOrder inOrder = inOrder(systemCommandStateChangedEventFirer, logger, publishedEventRebuilder);

        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());
        inOrder.verify(logger).info("REBUILD started at Fri May 24 12:00:00 Z 2019");
        inOrder.verify(publishedEventRebuilder).rebuild();
        inOrder.verify(logger).error("REBUILD failed", nullPointerException);
        inOrder.verify(systemCommandStateChangedEventFirer).fire(systemCommandStateChangedEventCaptor.capture());

        final List<SystemCommandStateChangedEvent> allValues = systemCommandStateChangedEventCaptor.getAllValues();

        final SystemCommandStateChangedEvent startEvent = allValues.get(0);
        final SystemCommandStateChangedEvent endEvent = allValues.get(1);

        assertThat(startEvent.getCommandId(), is(commandId));
        assertThat(startEvent.getCommandState(), is(COMMAND_IN_PROGRESS));
        assertThat(startEvent.getSystemCommand(), is(rebuildCommand));
        assertThat(startEvent.getStatusChangedAt(), is(rebuildStartedAt));
        assertThat(startEvent.getMessage(), is("REBUILD started at Fri May 24 12:00:00 Z 2019"));

        assertThat(endEvent.getCommandId(), is(commandId));
        assertThat(endEvent.getCommandState(), is(COMMAND_FAILED));
        assertThat(endEvent.getSystemCommand(), is(rebuildCommand));
        assertThat(endEvent.getStatusChangedAt(), is(rebuildCompletedAt));
        assertThat(endEvent.getMessage(), is("Rebuild failed: NullPointerException: Ooops"));
    }
}
