package uk.gov.justice.services.eventsourcing.util.io;

public class Closer {

    public void closeQuietly(final AutoCloseable autoCloseable) {

        if (autoCloseable == null) {
            return;
        }

        try {
            autoCloseable.close();
        } catch (final Exception ignored) {
        }
    }
}
