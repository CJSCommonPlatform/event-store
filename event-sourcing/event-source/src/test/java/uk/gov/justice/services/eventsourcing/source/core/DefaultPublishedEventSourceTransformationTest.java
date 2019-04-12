package uk.gov.justice.services.eventsourcing.source.core;

import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventsourcing.publishedevent.ActiveStreamsProcessor;
import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventsProcessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPublishedEventSourceTransformationTest {

    @Mock
    private ActiveStreamsProcessor activeStreamsProcessor;

    @Mock
    private PublishedEventsProcessor publishedEventsProcessor;

    @InjectMocks
    private DefaultPublishedEventSourceTransformation defaultPublishedEventSourceTransformation;

    @Test
    public void shouldTruncatePublishedEvents() throws Exception {
        
        defaultPublishedEventSourceTransformation.truncate();
        
        verify(publishedEventsProcessor).truncatePublishedEvents();
    }

    @Test
    public void shouldPopulatePublishedEvents() throws Exception {

        defaultPublishedEventSourceTransformation.populate();

        verify(activeStreamsProcessor).populatePublishedEvents();
    }
}
