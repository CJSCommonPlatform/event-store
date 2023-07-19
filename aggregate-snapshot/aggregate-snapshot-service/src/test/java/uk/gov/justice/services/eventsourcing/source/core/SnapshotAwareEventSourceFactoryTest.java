package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SnapshotAwareEventSourceFactoryTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private SnapshotService snapshotService;

    @InjectMocks
    private SnapshotAwareEventSourceFactory snapshotAwareEventSourceFactory;

    @Test
    public void shouldCreateSnapshotAwareEventSource() throws Exception {

        final String eventSourceName = "eventSourceName";

        final EventSource eventSource = snapshotAwareEventSourceFactory.create(eventSourceName);

        assertThat(eventSource, is(instanceOf(SnapshotAwareEventSource.class)));

        assertThat(getValueOfField(eventSource, "eventStreamManager", EventStreamManager.class), is(eventStreamManager));
        assertThat(getValueOfField(eventSource, "eventRepository", EventRepository.class), is(eventRepository));
        assertThat(getValueOfField(eventSource, "snapshotService", SnapshotService.class), is(snapshotService));
        assertThat(getValueOfField(eventSource, "eventSourceName", String.class), is(eventSourceName));
    }
}
