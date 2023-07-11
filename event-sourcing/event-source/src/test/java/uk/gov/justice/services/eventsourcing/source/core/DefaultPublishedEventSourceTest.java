package uk.gov.justice.services.eventsourcing.source.core;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultPublishedEventSourceTest {

    @Mock
    private MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository;

    @InjectMocks
    private DefaultPublishedEventSource defaultPublishedEventSource;

    @Test
    public void shouldFindEventsByEventNumber() throws Exception {

        final long eventNumber = 972834L;

        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(multipleDataSourcePublishedEventRepository.findEventsSince(eventNumber)).thenReturn(Stream.of(publishedEvent));

        final List<PublishedEvent> envelopes = defaultPublishedEventSource.findEventsSince(eventNumber).collect(toList());

        assertThat(envelopes.size(), is(1));
        assertThat(envelopes.get(0), is(publishedEvent));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFindEventRange() throws Exception {

        final long fromEventNumber = 1L;
        final long toEventNumber = 10L;
        final MissingEventRange missingEventRange = new MissingEventRange(fromEventNumber, toEventNumber);
        final Stream streamOfEvents = mock(Stream.class);

        when(multipleDataSourcePublishedEventRepository.findEventRange(fromEventNumber, toEventNumber)).thenReturn(streamOfEvents);

        final Stream<PublishedEvent> eventRange = defaultPublishedEventSource.findEventRange(missingEventRange);

        assertThat(eventRange, is(streamOfEvents));
    }
}
