package uk.gov.justice.services.event.sourcing.subscription.jmx;

public class MBeanException extends RuntimeException {

    public MBeanException(final String message) {
        super(message);
    }
}
