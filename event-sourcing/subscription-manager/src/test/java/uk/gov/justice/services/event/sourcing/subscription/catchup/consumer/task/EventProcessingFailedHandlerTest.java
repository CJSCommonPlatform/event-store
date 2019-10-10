package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupProcessingOfEventFailedEvent;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;
import uk.gov.justice.services.jmx.api.command.EventCatchupCommand;

import java.util.UUID;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventProcessingFailedHandlerTest {

    @Mock
    private Event<CatchupProcessingOfEventFailedEvent> catchupProcessingOfEventFailedEventFirer;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventProcessingFailedHandler eventProcessingFailedHandler;

    @Captor
    private ArgumentCaptor<CatchupProcessingOfEventFailedEvent> catchupProcessingOfEventFailedEventCaptor;

    @Test
    public void shouldLogExceptionAndFireFailureEvent() throws Exception {

        final NullPointerException nullPointerException = new NullPointerException("Ooops");
        final UUID commandId = randomUUID();
        final String subscriptionName = "subscriptionName";
        final String metadata = "{some: metadata}";
        final UUID eventId = randomUUID();
        final CatchupCommand catchupCommand = new EventCatchupCommand();

        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(publishedEvent.getId()).thenReturn(eventId);
        when(publishedEvent.getMetadata()).thenReturn(metadata);

        eventProcessingFailedHandler.handle(
                nullPointerException,
                publishedEvent,
                subscriptionName,
                catchupCommand,
                commandId);

        verify(logger).error("Failed to process publishedEvent with metadata: {some: metadata}", nullPointerException);

        verify(catchupProcessingOfEventFailedEventFirer).fire(catchupProcessingOfEventFailedEventCaptor.capture());

        final CatchupProcessingOfEventFailedEvent catchupProcessingOfEventFailedEvent = catchupProcessingOfEventFailedEventCaptor.getValue();

        assertThat(catchupProcessingOfEventFailedEvent.getEventId(), is(eventId));
        assertThat(catchupProcessingOfEventFailedEvent.getMetadata(), is(metadata));
        assertThat(catchupProcessingOfEventFailedEvent.getCatchupCommand(), is(catchupCommand));
        assertThat(catchupProcessingOfEventFailedEvent.getException(), is(nullPointerException));
        assertThat(catchupProcessingOfEventFailedEvent.getSubscriptionName(), is(subscriptionName));
    }
}
