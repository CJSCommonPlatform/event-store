package uk.gov.justice.services.messaging.core.logging;

import uk.gov.justice.services.messaging.logging.LoggerUtils;
import uk.gov.justice.services.messaging.logging.TraceLogger;

import java.util.function.Supplier;

import org.slf4j.Logger;

public class DefaultTraceLogger implements TraceLogger {

    @Override
    public void trace(final Logger logger, final Supplier<String> supplier) {
        LoggerUtils.trace(logger, supplier);
    }
}
