package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.PrePublishQueueRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.PublishedEventException;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrePublishProcessorTest {

    @Mock
    private PrePublishQueueRepository prePublishQueueRepository;

    @Mock
    private EventPrePublisher eventPrePublisher;

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @InjectMocks
    private PrePublishProcessor prePublishProcessor;

    @Test
    public void shouldRunPrePublishIfAnEventIsAvailableForPublishing() throws Exception {

        final UUID eventId = UUID.randomUUID();
        final Event event = mock(Event.class);

        when(prePublishQueueRepository.popNextEventId()).thenReturn(of(eventId));
        when(eventJdbcRepository.findById(eventId)).thenReturn(of(event));

        assertThat(prePublishProcessor.prePublishNextEvent(), is(true));

        verify(eventPrePublisher).prePublish(event);
    }

    @Test
    public void shouldDoNothingIfNoEventIsAvailableForPublishing() throws Exception {

        when(prePublishQueueRepository.popNextEventId()).thenReturn(empty());

        assertThat(prePublishProcessor.prePublishNextEvent(), is(false));

        verifyZeroInteractions(eventPrePublisher);
    }

    @Test
    public void shouldThrowExceptionIfEventNotFoundInEventLogTable() throws Exception {

        final UUID eventId = UUID.randomUUID();

        when(prePublishQueueRepository.popNextEventId()).thenReturn(of(eventId));
        when(eventJdbcRepository.findById(eventId)).thenReturn(empty());

        try {
            prePublishProcessor.prePublishNextEvent();
        } catch (final PublishedEventException expected) {
            assertThat(expected.getMessage(), is("Failed to find Event with id '" + eventId + "'"));
        }

        verifyZeroInteractions(eventPrePublisher);
    }
}
