package uk.gov.justice.services.subscription;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Stream.empty;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;
import static uk.gov.justice.services.subscription.ProcessedEventTrackItemBuilder.processedEventTrackItem;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.stream.StreamCloseSpy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessedEventTrackingServiceTest {

    @Mock
    private ProcessedEventTrackingRepository processedEventTrackingRepository;

    @InjectMocks
    private ProcessedEventTrackingService processedEventTrackingService;

    @Test
    public void shouldStoreCurrentEventNumberLinkedToPreviousEvent() throws Exception {

        final long previousEventNumber = 23;
        final long eventNumber = 24;

        final UUID id = randomUUID();
        final String source = "example-context";

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder()
                        .withId(id)
                        .withName("event-name")
                        .withPreviousEventNumber(previousEventNumber)
                        .withEventNumber(eventNumber)
                        .withSource(source),
                createObjectBuilder());

        processedEventTrackingService.trackProcessedEvent(event);

        verify(processedEventTrackingRepository).save(new ProcessedEventTrackItem(previousEventNumber, eventNumber, source));
    }

    @Test
    public void shouldThrowExceptionIfNoPreviousEventNumberFound() throws Exception {

        final long eventNumber = 24;

        final UUID id = fromString("8df3a64b-589e-4c55-adc1-b767ddbab42f");
        final String source = "example-context";

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder()
                        .withId(id)
                        .withName("event-name")
                        .withEventNumber(eventNumber)
                        .withSource(source),
                createObjectBuilder());

        try {
            processedEventTrackingService.trackProcessedEvent(event);
            fail();
        } catch (final ProcessedEventTrackingException expected) {
            assertThat(expected.getMessage(), is("Missing previous event number for event with id '8df3a64b-589e-4c55-adc1-b767ddbab42f'"));
        }
    }

    @Test
    public void shouldThrowExceptionIfNoEventNumberFound() throws Exception {

        final long previousEventNumber = 23;

        final UUID id = fromString("8df3a64b-589e-4c55-adc1-b767ddbab42f");
        final String source = "example-context";

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder()
                        .withId(id)
                        .withName("event-name")
                        .withPreviousEventNumber(previousEventNumber)
                        .withSource(source),
                createObjectBuilder());

        try {
            processedEventTrackingService.trackProcessedEvent(event);
            fail();
        } catch (final ProcessedEventTrackingException expected) {
            assertThat(expected.getMessage(), is("Missing event number for event with id '8df3a64b-589e-4c55-adc1-b767ddbab42f'"));
        }
    }

    @Test
    public void shouldThrowExceptionIfNoSourceFound() throws Exception {

        final long previousEventNumber = 23;
        final long eventNumber = 24;

        final UUID id = fromString("8df3a64b-589e-4c55-adc1-b767ddbab42f");

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder()
                        .withId(id)
                        .withName("event-name")
                        .withPreviousEventNumber(previousEventNumber)
                        .withEventNumber(eventNumber),
                createObjectBuilder());

        try {
            processedEventTrackingService.trackProcessedEvent(event);
            fail();
        } catch (final ProcessedEventTrackingException expected) {
            assertThat(expected.getMessage(), is("No source found in event with id '8df3a64b-589e-4c55-adc1-b767ddbab42f'"));
        }
    }

    @Test
    public void shouldGetTheListOfAllMissingEvents() throws Exception {

        final String source = "example-context";

        final List<ProcessedEventTrackItem> processedEventTrackItems = asList(
                processedEventTrackItem()
                        .withEventNumber(7)
                        .withPreviousEventNumber(6)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(3)
                        .withPreviousEventNumber(2)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(2)
                        .withPreviousEventNumber(1)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(1)
                        .withPreviousEventNumber(0)
                        .withSource(source)
                        .build()
        );

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEventTrackItem> processedEventTrackItemStream = processedEventTrackItems.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getAllProcessedEvents(source)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source);

        assertThat(missingEventRanges.size(), is(1));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(4L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(6L));

        assertThat(streamCloseSpy.streamClosed(), is(true));
    }

    @Test
    public void shouldHandleMissingEventsFromZero() throws Exception {

        final String source = "example-context";

        final List<ProcessedEventTrackItem> processedEventTrackItems = asList(
                processedEventTrackItem()
                        .withEventNumber(25)
                        .withPreviousEventNumber(24)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(24)
                        .withPreviousEventNumber(23)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(20)
                        .withPreviousEventNumber(19)
                        .withSource(source)
                        .build()
        );

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEventTrackItem> processedEventTrackItemStream = processedEventTrackItems.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getAllProcessedEvents(source)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source);

        assertThat(missingEventRanges.size(), is(2));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(1L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(19L));
        assertThat(missingEventRanges.get(1).getMissingEventFrom(), is(21L));
        assertThat(missingEventRanges.get(1).getMissingEventTo(), is(23L));

        assertThat(streamCloseSpy.streamClosed(), is(true));
    }

    @Test
    public void shouldReturnNoMissingEventsIfNoEventsFound() throws Exception {

        final String source = "example-context";

        when(processedEventTrackingRepository.getAllProcessedEvents(source)).thenReturn(empty());

        assertThat(processedEventTrackingService.getAllMissingEvents(source), is(emptyList()));
    }

    @Test
    public void shouldReturnNoMissingEventsIfNoEventsAreActuallyMissing() throws Exception {

        final String source = "example-context";

        final List<ProcessedEventTrackItem> processedEventTrackItems = asList(
                processedEventTrackItem()
                        .withEventNumber(4)
                        .withPreviousEventNumber(3)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(3)
                        .withPreviousEventNumber(2)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(2)
                        .withPreviousEventNumber(1)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(1)
                        .withPreviousEventNumber(0)
                        .withSource(source)
                        .build()
        );

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEventTrackItem> processedEventTrackItemStream = processedEventTrackItems.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getAllProcessedEvents(source)).thenReturn(processedEventTrackItemStream);

        assertThat(processedEventTrackingService.getAllMissingEvents(source), is(emptyList()));

        assertThat(streamCloseSpy.streamClosed(), is(true));
    }

    @Test
    public void shouldReturnNoMissingEventsIfOnlyOneEventExists() throws Exception {

        final String source = "example-context";

        final List<ProcessedEventTrackItem> processedEventTrackItems = singletonList(
                processedEventTrackItem()
                        .withEventNumber(1)
                        .withPreviousEventNumber(0)
                        .withSource(source)
                        .build()
        );

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEventTrackItem> processedEventTrackItemStream = processedEventTrackItems.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getAllProcessedEvents(source)).thenReturn(processedEventTrackItemStream);

        assertThat(processedEventTrackingService.getAllMissingEvents(source), is(emptyList()));

        assertThat(streamCloseSpy.streamClosed(), is(true));
    }

    @Test
    public void shouldHandleARangeOfMissingEventsOfJustOneMissingEvent() throws Exception {

        final String source = "example-context";

        final List<ProcessedEventTrackItem> processedEventTrackItems = asList(
                processedEventTrackItem()
                        .withEventNumber(3)
                        .withPreviousEventNumber(2)
                        .withSource(source)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(1)
                        .withPreviousEventNumber(0)
                        .withSource(source)
                        .build()
        );

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEventTrackItem> processedEventTrackItemStream = processedEventTrackItems.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getAllProcessedEvents(source)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source);

        assertThat(missingEventRanges.size(), is(1));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(2L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(2L));

        assertThat(streamCloseSpy.streamClosed(), is(true));
    }

    @Test
    public void shouldReturnMissingEventIfPreviousEventNumberIsNotZero() throws Exception {

        final String source = "example-context";

        final List<ProcessedEventTrackItem> processedEventTrackItems = singletonList(
                processedEventTrackItem()
                        .withEventNumber(2)
                        .withPreviousEventNumber(1)
                        .withSource(source)
                        .build()
        );

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEventTrackItem> processedEventTrackItemStream = processedEventTrackItems.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getAllProcessedEvents(source)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source);

        assertThat(missingEventRanges.size(), is(1));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(1L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(1L));

        assertThat(streamCloseSpy.streamClosed(), is(true));
    }

    @Test
    public void shouldGetTheEventNumberOfTheLatestProcessedEvent() throws Exception {

        final String source = "example-context";

        final long latestEventNumber = 2384L;

        final ProcessedEventTrackItem processedEventTrackItem = mock(ProcessedEventTrackItem.class);
        when(processedEventTrackingRepository.getLatestProcessedEvent(source)).thenReturn(of(processedEventTrackItem));
        when(processedEventTrackItem.getEventNumber()).thenReturn(latestEventNumber);

        assertThat(processedEventTrackingService.getLatestProcessedEventNumber(source), is(latestEventNumber));
    }

    @Test
    public void shouldReturnZeroAsTheEventNumberOfTheLatestProcessedEventIfNoEventsProcessedYet() throws Exception {

        final String source = "example-context";

        when(processedEventTrackingRepository.getLatestProcessedEvent(source)).thenReturn(Optional.empty());

        assertThat(processedEventTrackingService.getLatestProcessedEventNumber(source), is(0L));
    }
}
