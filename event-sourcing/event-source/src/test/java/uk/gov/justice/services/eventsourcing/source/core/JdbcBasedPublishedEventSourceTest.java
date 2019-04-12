package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JdbcBasedPublishedEventSourceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventConverter eventConverter;

    @InjectMocks
    private JdbcBasedPublishedEventSource jdbcBasedPublishedEventSource;

    @Test
    public void shouldFindEventsByEventNumber() throws Exception {

        final long eventNumber = 972834L;

        final PublishedEvent linkedEvent = mock(PublishedEvent.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(eventRepository.findEventsSince(eventNumber)).thenReturn(Stream.of(linkedEvent));
        when(eventConverter.envelopeOf(linkedEvent)).thenReturn(jsonEnvelope);

        final List<JsonEnvelope> envelopes = jdbcBasedPublishedEventSource.findEventsSince(eventNumber).collect(toList());

        assertThat(envelopes.size(), is(1));
        assertThat(envelopes.get(0), is(jsonEnvelope));
    }
}