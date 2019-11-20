package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.util.HashSet;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ActiveEventFilterTest {

    @InjectMocks
    private ActiveEventFilter activeEventFilter;

    @Test
    public void shouldReturnTrueIfTheStreamIdOfTheEventIsInTheSetOfActiveStreamIds() throws Exception {

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final UUID streamId_3 = randomUUID();

        final HashSet<UUID> streamIds = newHashSet(streamId_1, streamId_2, streamId_3);

        final Event event = mock(Event.class);

        when(event.getStreamId()).thenReturn(streamId_2);

        assertThat(activeEventFilter.isActiveEvent(event, streamIds), is(true));
    }

    @Test
    public void shouldReturnFalseIfTheStreamIdOfTheEventIsNotInTheSetOfActiveStreamIds() throws Exception {

        final UUID streamId_1 = randomUUID();
        final UUID streamId_2 = randomUUID();
        final UUID streamId_3 = randomUUID();

        final HashSet<UUID> streamIds = newHashSet(streamId_1, streamId_2, streamId_3);

        final Event event = mock(Event.class);

        when(event.getStreamId()).thenReturn(randomUUID());

        assertThat(activeEventFilter.isActiveEvent(event, streamIds), is(false));
    }
}
