package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class EventNumberGetterTest {

    @InjectMocks
    private EventNumberGetter eventNumberGetter;

    @Test
    public void shouldGetTheEvenNumberFromTheEvent() throws Exception {

        final long eventNumber = 23498723L;
        final Event event = mock(Event.class);

        when(event.getEventNumber()).thenReturn(of(eventNumber));

        assertThat(eventNumberGetter.eventNumberFrom(event), is(eventNumber));
    }

    @Test
    public void shouldThrowRebuildExceptionIfTheEventNumberIsNotPresent() throws Exception {

        final UUID eventId = fromString("285a4895-043c-4829-95d4-8918b2c473d2");
        final Event event = mock(Event.class);

        when(event.getEventNumber()).thenReturn(empty());
        when(event.getId()).thenReturn(eventId);

        try {
            eventNumberGetter.eventNumberFrom(event);
            fail();
        } catch (final RebuildException expected) {
            assertThat(expected.getMessage(), is("No eventNumber found for event with id '285a4895-043c-4829-95d4-8918b2c473d2'"));
        }
    }
}
