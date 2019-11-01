package uk.gov.justice.services.eventstore.management.verification.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventstore.management.verification.process.LinkedEventNumberTable.PROCESSED_EVENT;
import static uk.gov.justice.services.eventstore.management.verification.process.LinkedEventNumberTable.PUBLISHED_EVENT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventLinkageErrorMessageGeneratorTest {

    @InjectMocks
    private EventLinkageErrorMessageGenerator eventLinkageErrorMessageGenerator;

    @Test
    public void shouldGenerateTheCorrectErrorMessageForPublishedEvent() throws Exception {

        final int previousEventNumber = 23;
        final int currentEventNumber = 24;
        final int lastEvenNumber = 13;

        final String errorMessage = eventLinkageErrorMessageGenerator.generateErrorMessage(
                previousEventNumber,
                currentEventNumber,
                lastEvenNumber,
                PUBLISHED_EVENT);

        assertThat(errorMessage, is("Events incorrectly linked in published_event table: " +
                "Event with event number 24 is linked to previous event number 23 " +
                "whereas it should be 13"));
    }

    @Test
    public void shouldGenerateTheCorrectErrorMessageForProcessedEvent() throws Exception {

        final int previousEventNumber = 41;
        final int currentEventNumber = 42;
        final int lastEvenNumber = 23;

        final String errorMessage = eventLinkageErrorMessageGenerator.generateErrorMessage(
                previousEventNumber,
                currentEventNumber,
                lastEvenNumber,
                PROCESSED_EVENT);

        assertThat(errorMessage, is("Events missing from processed_event table: " +
                "Event with event_number 42 has a previous_event_number of 41, " +
                "but the event in the previous row in the database has an event_number of 23"));
    }
}
