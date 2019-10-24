package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.sourcing.subscription.manager.PublishedEventSourceProvider;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;
import uk.gov.justice.services.subscription.ProcessedEventTrackingService;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MissingEventStreamerTest {

    @Mock
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Mock
    private ProcessedEventTrackingService processedEventTrackingService;

    @InjectMocks
    private MissingEventStreamer missingEventStreamer;

    @Test
    public void shouldFindTheRangesOfMissingEventsAndStreamThemAsPublishedEvents() throws Exception {

        final String componentName = "EVENT_LISTENER";
        final String eventSourceName = "event source name";

        final PublishedEventSource publishedEventSource = mock(PublishedEventSource.class);

        final MissingEventRange missingEventRange_1 = mock(MissingEventRange.class);
        final MissingEventRange missingEventRange_2 = mock(MissingEventRange.class);

        final Stream<MissingEventRange> missingEventRangeStream = Stream.of(missingEventRange_1, missingEventRange_2);

        final PublishedEvent publishedEvent_2 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_3 = mock(PublishedEvent.class);

        final PublishedEvent publishedEvent_7 = mock(PublishedEvent.class);
        final PublishedEvent publishedEvent_8 = mock(PublishedEvent.class);

        final Stream<PublishedEvent> publishedEventStream_1 = Stream.of(publishedEvent_2, publishedEvent_3);
        final Stream<PublishedEvent> publishedEventStream_2 = Stream.of(publishedEvent_7, publishedEvent_8);


        when(publishedEventSourceProvider.getPublishedEventSource(eventSourceName)).thenReturn(publishedEventSource);
        when(processedEventTrackingService.getAllMissingEvents(eventSourceName, componentName)).thenReturn(missingEventRangeStream);
        when(publishedEventSource.findEventRange(missingEventRange_1)).thenReturn(publishedEventStream_1);
        when(publishedEventSource.findEventRange(missingEventRange_2)).thenReturn(publishedEventStream_2);

        final List<PublishedEvent> missingEvents = missingEventStreamer.getMissingEvents(eventSourceName, componentName)
                .collect(toList());

        assertThat(missingEvents.size(), is(4));
        assertThat(missingEvents.get(0), is(publishedEvent_2));
        assertThat(missingEvents.get(1), is(publishedEvent_3));
        assertThat(missingEvents.get(2), is(publishedEvent_7));
        assertThat(missingEvents.get(3), is(publishedEvent_8));
    }
}
