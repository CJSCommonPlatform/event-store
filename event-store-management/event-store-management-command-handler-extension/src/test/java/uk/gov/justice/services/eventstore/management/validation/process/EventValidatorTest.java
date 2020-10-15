package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EventValidatorTest {

    @Mock
    private PublishedEventSource publishedEventSource;

    @Mock
    private SingleEventValidator singleEventValidator;

    @InjectMocks
    private EventValidator eventValidator;

    @Test
    public void shouldGetAllPublishedEventsAndReturnAnyValidationErrors() throws Exception {

        final PublishedEvent successfulEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent failedEvent_1 = mock(PublishedEvent.class);
        final PublishedEvent successfulEvent_2 = mock(PublishedEvent.class);
        final PublishedEvent failedEvent_2 = mock(PublishedEvent.class);

        final ValidationError validationError_1 = mock(ValidationError.class);
        final ValidationError validationError_2 = mock(ValidationError.class);


        when(publishedEventSource.findEventsSince(0L)).thenReturn(Stream.of(
                successfulEvent_1,
                failedEvent_1,
                successfulEvent_2,
                failedEvent_2
        ));

        when(singleEventValidator.validate(successfulEvent_1)).thenReturn(empty());
        when(singleEventValidator.validate(failedEvent_1)).thenReturn(of(validationError_1));
        when(singleEventValidator.validate(successfulEvent_2)).thenReturn(empty());
        when(singleEventValidator.validate(failedEvent_2)).thenReturn(of(validationError_2));

        final List<ValidationError> validatorErrors = eventValidator.findErrors();

        assertThat(validatorErrors.size(), is(2));
        assertThat(validatorErrors, hasItem(validationError_1));
        assertThat(validatorErrors, hasItem(validationError_2));
    }
}
