package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class RebuildPublishedEventFactoryTest {

    @Mock
    private EventNumberGetter eventNumberGetter;

    @Mock
    private PublishedEventConverter publishedEventConverter;

    @InjectMocks
    private RebuildPublishedEventFactory rebuildPublishedEventFactory;

    @Test
    public void shouldCreatePublishedEventAndUpdatePreviousEventNumber() throws Exception {

        final long previousEventNumber = 23L;
        final long eventNumber = 24L;

        final AtomicLong previousEventNumberAtomicLong = new AtomicLong(previousEventNumber);

        final Event event = mock(Event.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);

        when(eventNumberGetter.eventNumberFrom(event)).thenReturn(eventNumber);
        when(publishedEventConverter.toPublishedEvent(
                event,
                previousEventNumber)).thenReturn(publishedEvent);

        assertThat(rebuildPublishedEventFactory.createPublishedEventFrom(
                event,
                previousEventNumberAtomicLong), is(publishedEvent));

        assertThat(previousEventNumberAtomicLong.get(), is(eventNumber));

    }
}
