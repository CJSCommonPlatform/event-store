package uk.gov.justice.services.eventstore.management.validation.commands;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.validation.process.CatchupVerificationProcess;
import uk.gov.justice.services.jmx.api.command.VerifyCatchupCommand;
import uk.gov.justice.services.jmx.logging.MdcLogger;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class VerifyCatchupCommandHandlerTest {

    @Mock
    private CatchupVerificationProcess catchupVerificationProcess;

    @Mock
    private MdcLogger mdcLogger;

    @Mock
    private Logger logger;

    private Consumer<Runnable> testConsumer = Runnable::run;

    @InjectMocks
    private VerifyCatchupCommandHandler verifyCatchupCommandHandler;

    @Test
    public void shouldRunTheVerificationProcess() throws Exception {

        final UUID commandId = UUID.randomUUID();
        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);

        verifyCatchupCommandHandler.validateCatchup(new VerifyCatchupCommand(), commandId);

        final InOrder inOrder = inOrder(logger, catchupVerificationProcess);

        inOrder.verify(logger).info("Received VERIFY_CATCHUP command");
        inOrder.verify(catchupVerificationProcess).runVerification();
    }
}
