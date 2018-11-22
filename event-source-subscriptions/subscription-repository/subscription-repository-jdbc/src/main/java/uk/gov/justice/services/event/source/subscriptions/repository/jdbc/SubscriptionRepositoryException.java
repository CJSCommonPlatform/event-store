package uk.gov.justice.services.event.source.subscriptions.repository.jdbc;

public class SubscriptionRepositoryException extends RuntimeException {

    public SubscriptionRepositoryException(final String message) {
        super(message);
    }

    public SubscriptionRepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
