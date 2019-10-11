package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.rebuild.process.RebuildProcessRunner;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;
import uk.gov.justice.services.jmx.logging.MdcLogger;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RebuildCommandHandlerTest {

    @Mock
    private RebuildProcessRunner rebuildProcessRunner;

    @Mock
    private MdcLogger mdcLogger;

    @InjectMocks
    private RebuildCommandHandler rebuildCommandHandler;

    private Consumer<Runnable> testConsumer = Runnable::run;

    @Test
    public void shouldFireRebuildEvent() throws Exception {

        final UUID commandId = randomUUID();
        final RebuildCommand rebuildCommand = new RebuildCommand();

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        rebuildCommandHandler.doRebuild(rebuildCommand, commandId);

        verify(rebuildProcessRunner).runRebuild(commandId, rebuildCommand);
    }
}
