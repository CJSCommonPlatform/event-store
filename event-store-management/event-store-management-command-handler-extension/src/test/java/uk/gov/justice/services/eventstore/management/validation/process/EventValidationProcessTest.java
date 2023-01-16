package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;

import uk.gov.justice.services.eventstore.management.CommandResult;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EventValidationProcessTest {

    @Mock
    private EventValidator eventValidator;

    @InjectMocks
    private EventValidationProcess eventValidationProcess;

    @Test
    public void shouldReturnSuccessIfAllPublishedEventsPassValidation() throws Exception {

        final UUID commandId = fromString("4007249d-8e5a-49d2-bdbb-1d8a960baac5");

        when(eventValidator.findErrors()).thenReturn(emptyList());

        final CommandResult commandResult = eventValidationProcess.validateAllPublishedEvents(commandId);

        assertThat(commandResult.getCommandState(), is(COMMAND_COMPLETE));
        assertThat(commandResult.getCommandId(), is(commandId));
        assertThat(commandResult.getMessage(), is("All PublishedEvents successfully passed schema validation"));
    }

    @Test
    public void shouldReturnFailureIfAnyPublishedEventsFailValidation() throws Exception {

        final UUID commandId = fromString("4007249d-8e5a-49d2-bdbb-1d8a960baac5");

        final ValidationError validationError_1 = mock(ValidationError.class);
        final ValidationError validationError_2 = mock(ValidationError.class);

        when(eventValidator.findErrors()).thenReturn(asList(validationError_1, validationError_2));

        final CommandResult commandResult = eventValidationProcess.validateAllPublishedEvents(commandId);

        assertThat(commandResult.getCommandState(), is(COMMAND_FAILED));
        assertThat(commandResult.getCommandId(), is(commandId));
        assertThat(commandResult.getMessage(), is("2 PublishedEvent(s) failed schema validation. Please see server logs for errors"));
    }
}
