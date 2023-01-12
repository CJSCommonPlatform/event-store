package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JdbcEventSourceFactoryTest {

    @Mock
    private EventStreamManager eventStreamManager;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private JdbcEventSourceFactory jdbcEventSourceFactory;

    @Test
    public void shouldCreateJdbcBasedEventSource() throws Exception {

        final String eventSourceName = "eventSourceName";

        final JdbcBasedEventSource jdbcBasedEventSource = jdbcEventSourceFactory.create(eventSourceName);

        assertThat(getValueOfField(jdbcBasedEventSource, "eventStreamManager", EventStreamManager.class), is(eventStreamManager));
        assertThat(getValueOfField(jdbcBasedEventSource, "eventRepository", EventRepository.class), is(eventRepository));
        assertThat(getValueOfField(jdbcBasedEventSource, "eventSourceName", String.class), is(eventSourceName));
    }
}
