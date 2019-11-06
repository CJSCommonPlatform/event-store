package uk.gov.justice.services.subscription;

import static java.lang.Long.MAX_VALUE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
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
import static uk.gov.justice.services.subscription.ProcessedEventBuilder.processedEventTrackItem;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;
import uk.gov.justice.services.eventsourcing.util.messaging.EventSourceNameCalculator;
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
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ProcessedEventTrackingServiceTest {

    @Mock
    private ProcessedEventTrackingRepository processedEventTrackingRepository;

    @Mock
    private EventSourceNameCalculator eventSourceNameCalculator;

    @Mock
    private Logger logger;

    @InjectMocks
    private ProcessedEventTrackingService processedEventTrackingService;

    @Test
    public void shouldStoreCurrentEventNumberLinkedToPreviousEvent() throws Exception {

        final long previousEventNumber = 23;
        final long eventNumber = 24;

        final UUID eventId = randomUUID();
        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder()
                        .withId(eventId)
                        .withName("event-name")
                        .withPreviousEventNumber(previousEventNumber)
                        .withEventNumber(eventNumber)
                        .withSource(source),
                createObjectBuilder());

        when(eventSourceNameCalculator.getSource(event)).thenReturn(source);

        processedEventTrackingService.trackProcessedEvent(event, componentName);

        verify(processedEventTrackingRepository).save(new ProcessedEvent(eventId, previousEventNumber, eventNumber, source, componentName));
    }

    @Test
    public void shouldThrowExceptionIfNoPreviousEventNumberFound() throws Exception {

        final long eventNumber = 24;

        final UUID id = fromString("8df3a64b-589e-4c55-adc1-b767ddbab42f");
        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder()
                        .withId(id)
                        .withName("event-name")
                        .withEventNumber(eventNumber)
                        .withSource(source),
                createObjectBuilder());

        try {
            processedEventTrackingService.trackProcessedEvent(event, componentName);
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
        final String componentName = "EVENT_LISTENER";

        final JsonEnvelope event = envelopeFrom(
                metadataBuilder()
                        .withId(id)
                        .withName("event-name")
                        .withPreviousEventNumber(previousEventNumber)
                        .withSource(source),
                createObjectBuilder());

        try {
            processedEventTrackingService.trackProcessedEvent(event, componentName);
            fail();
        } catch (final ProcessedEventTrackingException expected) {
            assertThat(expected.getMessage(), is("Missing event number for event with id '8df3a64b-589e-4c55-adc1-b767ddbab42f'"));
        }
    }

    @Test
    public void shouldGetTheListOfAllMissingEvents() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final List<ProcessedEvent> processedEvents = asList(
                processedEventTrackItem()
                        .withEventNumber(7)
                        .withPreviousEventNumber(6)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(3)
                        .withPreviousEventNumber(2)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(2)
                        .withPreviousEventNumber(1)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(1)
                        .withPreviousEventNumber(0)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build()
        );

        final ProcessedEvent latestProcessedEvent = processedEventTrackItem()
                .withEventNumber(7)
                .withPreviousEventNumber(6)
                .withSource(source)
                .withComponentName(componentName)
                .build();

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEvent> processedEventTrackItemStream = processedEvents.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(of(latestProcessedEvent));
        when(processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source, componentName)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(2));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(4L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(7L));
        assertThat(missingEventRanges.get(1).getMissingEventFrom(), is(8L));
        assertThat(missingEventRanges.get(1).getMissingEventTo(), is(MAX_VALUE));

        assertThat(streamCloseSpy.streamClosed(), is(true));

        verify(logger).info("Missing Event Ranges: [\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 4, missingEventTo (exclusive) = 7},\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 8, missingEventTo (exclusive) = " + MAX_VALUE + "}\n" +
                "]"
        );
    }

    @Test
    public void shouldHandleMissingEventsFromZero() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final List<ProcessedEvent> processedEvents = asList(
                processedEventTrackItem()
                        .withPreviousEventNumber(24)
                        .withEventNumber(25)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withPreviousEventNumber(23)
                        .withEventNumber(24)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withPreviousEventNumber(19)
                        .withEventNumber(20)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build()
        );

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEvent> processedEventTrackItemStream = processedEvents.stream().onClose(streamCloseSpy);

        final ProcessedEvent processedEvent = processedEventTrackItem()
                .withPreviousEventNumber(24)
                .withEventNumber(25)
                .withSource(source)
                .withComponentName(componentName)
                .build();

        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(of(processedEvent));
        when(processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source, componentName)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(3));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(1L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(20L));
        assertThat(missingEventRanges.get(1).getMissingEventFrom(), is(21L));
        assertThat(missingEventRanges.get(1).getMissingEventTo(), is(24L));
        assertThat(missingEventRanges.get(2).getMissingEventFrom(), is(26L));
        assertThat(missingEventRanges.get(2).getMissingEventTo(), is(MAX_VALUE));

        assertThat(streamCloseSpy.streamClosed(), is(true));

        verify(logger).info("Missing Event Ranges: [\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 1, missingEventTo (exclusive) = 20},\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 21, missingEventTo (exclusive) = 24},\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 26, missingEventTo (exclusive) = " + MAX_VALUE + "}\n" +
                "]"
        );
    }

    @Test
    public void shouldReturnRangeOfOneToMaxLongIfNoEventsFound() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(Optional.empty());
        when(processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName)).thenReturn(empty());

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source, componentName)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(1));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(1L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(MAX_VALUE));

        verify(logger).info("Missing Event Ranges: [\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 1, missingEventTo (exclusive) = " + MAX_VALUE + "}\n" +
                "]"
        );
    }

    @Test
    public void shouldReturnNoMissingEventsIfNoEventsAreActuallyMissing() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final List<ProcessedEvent> processedEvents = asList(
                processedEventTrackItem()
                        .withEventNumber(4)
                        .withPreviousEventNumber(3)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(3)
                        .withPreviousEventNumber(2)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(2)
                        .withPreviousEventNumber(1)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(1)
                        .withPreviousEventNumber(0)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build()
        );

        final ProcessedEvent latestProcessedEvent = processedEventTrackItem()
                .withEventNumber(4)
                .withPreviousEventNumber(3)
                .withSource(source)
                .withComponentName(componentName)
                .build();

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEvent> processedEventTrackItemStream = processedEvents.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(of(latestProcessedEvent));
        when(processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source, componentName)
                .collect(toList());
        assertThat(missingEventRanges.size(), is(1));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(5L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(MAX_VALUE));

        assertThat(streamCloseSpy.streamClosed(), is(true));

        verify(logger).info("Missing Event Ranges: [\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 5, missingEventTo (exclusive) = " + MAX_VALUE + "}\n" +
                "]"
        );
    }

    @Test
    public void shouldReturnNoMissingEventsIfOnlyOneEventExists() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final List<ProcessedEvent> processedEvents = singletonList(
                processedEventTrackItem()
                        .withEventNumber(1)
                        .withPreviousEventNumber(0)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build()
        );

        final ProcessedEvent latestProcessedEvent = processedEventTrackItem()
                .withEventNumber(1)
                .withPreviousEventNumber(0)
                .withSource(source)
                .withComponentName(componentName)
                .build();

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEvent> processedEventTrackItemStream = processedEvents.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(of(latestProcessedEvent));
        when(processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source, componentName)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(1));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(2L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(MAX_VALUE));

        assertThat(streamCloseSpy.streamClosed(), is(true));

        verify(logger).info("Missing Event Ranges: [\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 2, missingEventTo (exclusive) = " + MAX_VALUE + "}\n" +
                "]"
        );
    }

    @Test
    public void shouldHandleARangeOfMissingEventsOfJustOneMissingEvent() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final List<ProcessedEvent> processedEvents = asList(
                processedEventTrackItem()
                        .withEventNumber(3)
                        .withPreviousEventNumber(2)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build(),
                processedEventTrackItem()
                        .withEventNumber(1)
                        .withPreviousEventNumber(0)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build()
        );

        final ProcessedEvent latestProcessedEvent = processedEventTrackItem()
                .withEventNumber(3)
                .withPreviousEventNumber(2)
                .withSource(source)
                .withComponentName(componentName)
                .build();

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEvent> processedEventTrackItemStream = processedEvents.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(of(latestProcessedEvent));
        when(processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source, componentName)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(2));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(2L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(3L));
        assertThat(missingEventRanges.get(1).getMissingEventFrom(), is(4L));
        assertThat(missingEventRanges.get(1).getMissingEventTo(), is(MAX_VALUE));

        assertThat(streamCloseSpy.streamClosed(), is(true));

        verify(logger).info("Missing Event Ranges: [\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 2, missingEventTo (exclusive) = 3},\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 4, missingEventTo (exclusive) = " + MAX_VALUE + "}\n" +
                "]"
        );
    }

    @Test
    public void shouldReturnMissingEventIfPreviousEventNumberIsNotZero() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final List<ProcessedEvent> processedEvents = singletonList(
                processedEventTrackItem()
                        .withEventNumber(2)
                        .withPreviousEventNumber(1)
                        .withSource(source)
                        .withComponentName(componentName)
                        .build()
        );

        final ProcessedEvent latestProcessedEvent = processedEventTrackItem()
                .withEventNumber(2)
                .withPreviousEventNumber(1)
                .withSource(source)
                .withComponentName(componentName)
                .build();

        final StreamCloseSpy streamCloseSpy = new StreamCloseSpy();
        final Stream<ProcessedEvent> processedEventTrackItemStream = processedEvents.stream().onClose(streamCloseSpy);

        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(of(latestProcessedEvent));
        when(processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName)).thenReturn(processedEventTrackItemStream);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService.getAllMissingEvents(source, componentName)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(2));

        assertThat(missingEventRanges.get(0).getMissingEventFrom(), is(1L));
        assertThat(missingEventRanges.get(0).getMissingEventTo(), is(2L));
        assertThat(missingEventRanges.get(1).getMissingEventFrom(), is(3L));
        assertThat(missingEventRanges.get(1).getMissingEventTo(), is(MAX_VALUE));

        assertThat(streamCloseSpy.streamClosed(), is(true));

        verify(logger).info("Missing Event Ranges: [\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 1, missingEventTo (exclusive) = 2},\n" +
                "MissingEventRange{missingEventFrom (inclusive) = 3, missingEventTo (exclusive) = " + MAX_VALUE + "}\n" +
                "]"
        );
    }

    @Test
    public void shouldGetTheEventNumberOfTheLatestProcessedEvent() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final long latestEventNumber = 2384L;

        final ProcessedEvent processedEvent = mock(ProcessedEvent.class);
        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(of(processedEvent));
        when(processedEvent.getEventNumber()).thenReturn(latestEventNumber);

        assertThat(processedEventTrackingService.getLatestProcessedEventNumber(source, componentName), is(latestEventNumber));
    }

    @Test
    public void shouldReturnZeroAsTheEventNumberOfTheLatestProcessedEventIfNoEventsProcessedYet() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        when(processedEventTrackingRepository.getLatestProcessedEvent(source, componentName)).thenReturn(Optional.empty());

        assertThat(processedEventTrackingService.getLatestProcessedEventNumber(source, componentName), is(0L));
    }
}
