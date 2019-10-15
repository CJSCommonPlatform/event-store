package uk.gov.justice.services.eventstore.management.validation.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.command.VerifyCatchupCommand.VERIFY_CATCHUP;

import uk.gov.justice.services.eventstore.management.validation.process.CatchupVerificationProcessRunner;
import uk.gov.justice.services.jmx.api.command.VerifyCatchupCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;

import java.util.UUID;

import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

public class VerifyCatchupCommandHandler {

    @Inject
    private CatchupVerificationProcessRunner catchupVerificationProcessRunner;

    @Inject
    private Logger logger;

    @Interceptors(MdcLoggerInterceptor.class)
    @HandlesSystemCommand(VERIFY_CATCHUP)
    public void validateCatchup(final VerifyCatchupCommand verifyCatchupCommand, final UUID commandId) {

        logger.info(format("Received %s command", verifyCatchupCommand.getName()));
        catchupVerificationProcessRunner.runVerificationProcess(commandId, verifyCatchupCommand);
    }
}
