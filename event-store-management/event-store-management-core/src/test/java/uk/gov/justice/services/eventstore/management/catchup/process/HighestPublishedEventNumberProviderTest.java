package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class HighestPublishedEventNumberProviderTest {

    @Mock
    private PublishedEventRepository publishedEventRepository;

    @InjectMocks
    private HighestPublishedEventNumberProvider highestPublishedEventNumberProvider;

    @Test
    public void shouldGetTheEventNumberOfTheLatestEvent() throws Exception {

        final long eventNumber = 23L;
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(publishedEventRepository.getLatestPublishedEvent()).thenReturn(of(publishedEvent));
        when(publishedEvent.getEventNumber()).thenReturn(of(eventNumber));

        assertThat(highestPublishedEventNumberProvider.getHighestPublishedEventNumber(), is(eventNumber));
    }

    @Test
    public void shouldReturnZeroIfNoEventsExist() throws Exception {

        when(publishedEventRepository.getLatestPublishedEvent()).thenReturn(empty());

        assertThat(highestPublishedEventNumberProvider.getHighestPublishedEventNumber(), is(0L));
    }

    @Test
    public void shouldReturnZeroIfThePublishedEventHasNoEventNumber() throws Exception {

        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(publishedEventRepository.getLatestPublishedEvent()).thenReturn(of(publishedEvent));
        when(publishedEvent.getEventNumber()).thenReturn(empty());

        assertThat(highestPublishedEventNumberProvider.getHighestPublishedEventNumber(), is(0L));
    }
}