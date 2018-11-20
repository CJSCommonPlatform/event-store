package uk.gov.justice.services.eventsourcing.prepublish;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.EventDeQueuer.PRE_PUBLISH_TABLE_NAME;

import uk.gov.justice.services.eventsourcing.EventDeQueuer;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

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
    private EventPrePublisher prePublishDelegate;

    @InjectMocks
    private PrePublishProcessor prePublishProcessor;

    @Test
    public void shouldRunPrePublishIfAnEventIsAvailableForPublishing() throws Exception {

        final Event event = mock(Event.class);

        when(eventDeQueuer.popNextEvent(PRE_PUBLISH_TABLE_NAME)).thenReturn(of(event));

        assertThat(prePublishProcessor.prePublishNextEvent(), is(true));

        verify(prePublishDelegate).prePublish(event);
    }

    @Test
    public void shouldDoNothingIfNoEventIsAvailableForPublishing() throws Exception {

        when(eventDeQueuer.popNextEvent(PRE_PUBLISH_TABLE_NAME)).thenReturn(empty());

        assertThat(prePublishProcessor.prePublishNextEvent(), is(false));

        verifyZeroInteractions(prePublishDelegate);
    }
}
