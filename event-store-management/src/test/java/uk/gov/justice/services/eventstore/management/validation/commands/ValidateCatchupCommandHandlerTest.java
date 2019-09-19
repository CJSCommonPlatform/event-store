package uk.gov.justice.services.eventstore.management.validation.commands;

import static org.mockito.Mockito.verify;

import uk.gov.justice.services.jmx.api.command.ValidateCatchupCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ValidateCatchupCommandHandlerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private ValidateCatchupCommandHandler validateCatchupCommandHandler;

    @Test
    public void shouldLogNotImplemented() throws Exception {

        validateCatchupCommandHandler.validateCatchup(new ValidateCatchupCommand());

        verify(logger).warn("Command VALIDATE_CATCHUP not yet implemented");
    }
}
