package uk.gov.justice.services.eventsourcing.source.core;

import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventsourcing.publishedevent.ActiveStreamsRepublisher;
import uk.gov.justice.services.eventsourcing.publishedevent.publish.PublishedEventsProcessor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultPublishedEventSourceTransformationTest {

    @Mock
    private ActiveStreamsRepublisher activeStreamsRepublisher;

    @Mock
    private PublishedEventsProcessor publishedEventsProcessor;

    @InjectMocks
    private DefaultPublishedEventSourceTransformation defaultPublishedEventSourceTransformation;

    @Test
    public void shouldTruncatePublishedEvents() throws Exception {
        
        defaultPublishedEventSourceTransformation.deleteAllPublishedEvents();
        
        verify(publishedEventsProcessor).truncatePublishedEvents();
    }

    @Test
    public void shouldPopulatePublishedEvents() throws Exception {

        defaultPublishedEventSourceTransformation.populatePublishedEvents();

        verify(activeStreamsRepublisher).populatePublishedEvents();
    }
}
