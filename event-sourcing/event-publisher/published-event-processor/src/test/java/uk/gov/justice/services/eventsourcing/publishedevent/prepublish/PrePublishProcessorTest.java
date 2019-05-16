package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.EventDeQueuer.PRE_PUBLISH_TABLE_NAME;

import uk.gov.justice.services.eventsourcing.publishedevent.PublishedEventException;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.EventDeQueuer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrePublishProcessorTest {

    @Mock
    private EventDeQueuer eventDeQueuer;

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

        when(eventDeQueuer.popNextEventId(PRE_PUBLISH_TABLE_NAME)).thenReturn(of(eventId));
        when(eventJdbcRepository.findById(eventId)).thenReturn(of(event));

        assertThat(prePublishProcessor.prePublishNextEvent(), is(true));

        verify(eventPrePublisher).prePublish(event);
    }

    @Test
    public void shouldDoNothingIfNoEventIsAvailableForPublishing() throws Exception {

        when(eventDeQueuer.popNextEventId(PRE_PUBLISH_TABLE_NAME)).thenReturn(empty());

        assertThat(prePublishProcessor.prePublishNextEvent(), is(false));

        verifyZeroInteractions(eventPrePublisher);
    }

    @Test
    public void shouldThrowExceptionIfEventNotFoundInEventLogTable() throws Exception {

        final UUID eventId = UUID.randomUUID();

        when(eventDeQueuer.popNextEventId(PRE_PUBLISH_TABLE_NAME)).thenReturn(of(eventId));
        when(eventJdbcRepository.findById(eventId)).thenReturn(empty());

        try {
            prePublishProcessor.prePublishNextEvent();
        } catch (final PublishedEventException expected) {
            assertThat(expected.getMessage(), is("Failed to find Event with id '" + eventId + "'"));
        }

        verifyZeroInteractions(eventPrePublisher);
    }
}
