package uk.gov.justice.services.eventsourcing.util.io;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CloserTest {

    @InjectMocks
    private Closer closer;

    @Test
    public void shouldCloseTheAutoCloseable() throws Exception {

        final AutoCloseable autoCloseable = mock(AutoCloseable.class);

        closer.closeQuietly(autoCloseable);

        verify(autoCloseable).close();
    }

    @Test
    public void shouldHandleNulls() throws Exception {

        closer.closeQuietly(null);
    }

    @Test
    public void shouldSwallowExceptions() throws Exception {

        final AutoCloseable autoCloseable = mock(AutoCloseable.class);

        doThrow(new SQLException("Ooops")).when(autoCloseable).close();

        closer.closeQuietly(autoCloseable);
    }
}
