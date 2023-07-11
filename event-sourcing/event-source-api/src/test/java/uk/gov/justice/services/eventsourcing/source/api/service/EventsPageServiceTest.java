package uk.gov.justice.services.eventsourcing.source.api.service;

import static java.net.URI.create;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.first;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.head;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Position.position;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Direction;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventEntry;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventsService;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Position;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventsPageServiceTest {

    private static final String BASE_URL = "http://server:123/context/";
    private static final String EVENT_STREAM_PATH = "event-streams/";

    @Mock
    private UrlLinkFactory urlLinkFactory;

    @Mock
    private EventsService service;

    @Mock
    private PositionFactory positionFactory;

    @InjectMocks
    private EventsPageService eventsPageService;

    @Test
    public void shouldReturnEventsWhenLessRecordsThanPageSizeWhenLookingForNewerEvents() throws Exception {

        final UUID streamId = randomUUID();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-st¬reams/" + streamId));

        final List<EventEntry> events = new ArrayList<>();

        final EventEntry event1 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", createObjectBuilder().add("field3", "value3").build(), new UtcClock().now().toString());

        events.add(event1);

        when(service.eventExists(streamId, 4L)).thenReturn(true);

        when(service.eventExists(streamId, 2L)).thenReturn(false);

        final Position position = position(3L);
        when(positionFactory.createPosition("3")).thenReturn(position);

        when(service.events(streamId, position, FORWARD, 2L)).thenReturn(events);

        final URL nextUrl = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/4/FORWARD/2");
        when(urlLinkFactory.createEventsUrlLink(position(4L), FORWARD, 2, uriInfo)).thenReturn(nextUrl);

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final Page<EventEntry> pageEvents = eventsPageService.pageEvents(streamId, "3", FORWARD, 2, uriInfo);

        final List<EventEntry> pageEventsData = pageEvents.getData();

        final PagingLinks pagingLinks = pageEvents.getPagingLinks();

        assertThat(pageEventsData, hasSize(1));

        assertThat(pageEventsData.get(0).getStreamId(), is(streamId.toString()));

        assertThat(pageEventsData.get(0).getPosition(), is(3L));

        assertThat(pageEventsData.get(0).getPayload(), is(notNullValue()));

        assertThat(pagingLinks.getNext().get(), is(nextUrl));
        assertThat(pagingLinks.getPrevious(), is(empty()));
        assertThat(pagingLinks.getHead(), is(headURL));
        assertThat(pagingLinks.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnEventsWhenLessRecordsThanPageSizeWhenLookingForOlderEvents() throws Exception {

        final UUID streamId = randomUUID();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL previousUrl = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/2/BACKWARD/2");
        when(urlLinkFactory.createEventsUrlLink(position(2L), BACKWARD, 2, uriInfo)).thenReturn(previousUrl);

        final List<EventEntry> events = new ArrayList<>();

        final EventEntry event1 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", createObjectBuilder().add("field3", "value3").build(), new UtcClock().now().toString());

        events.add(event1);

        when(service.eventExists(streamId, 4L)).thenReturn(false);

        when(service.eventExists(streamId, 2L)).thenReturn(true);

        final Position position = position(3L);
        when(positionFactory.createPosition("3")).thenReturn(position);
        when(service.events(streamId, position, BACKWARD, 2L)).thenReturn(events);

        final Page<EventEntry> feedActual = eventsPageService.pageEvents(streamId, "3", BACKWARD, 2, uriInfo);

        final List<EventEntry> feed = feedActual.getData();

        final PagingLinks pagingLinks = feedActual.getPagingLinks();

        assertThat(feed, hasSize(1));

        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));

        assertThat(feed.get(0).getPosition(), is(3L));

        assertThat(feed.get(0).getPayload(), is(notNullValue()));

        assertThat(pagingLinks.getPrevious().get(), is(previousUrl));
        assertThat(pagingLinks.getNext(), is(empty()));
        assertThat(pagingLinks.getHead(), is(headURL));
        assertThat(pagingLinks.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnFeedWhenSameNumberOfRecordsAsPageSizeWhenLookingForNewerEvents() throws Exception {
        final UUID streamId = randomUUID();

        final List<EventEntry> events = new ArrayList<>();

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final JsonObject payloadEvent1 = createObjectBuilder().add("field1", "value1").build();
        final JsonObject payloadEvent2 = createObjectBuilder().add("field2", "value2").build();

        final EventEntry event1 = new EventEntry(randomUUID(), streamId, 1L, "Test Name1", payloadEvent1, new UtcClock().now().toString());
        final EventEntry event2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", payloadEvent2, new UtcClock().now().toString());
        events.add(event2);
        events.add(event1);

        when(service.eventExists(streamId, 3L)).thenReturn(true);

        when(service.eventExists(streamId, 0L)).thenReturn(false);

        final URL nextUrl = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/3/FORWARD/2");
        when(urlLinkFactory.createEventsUrlLink(position(3L), FORWARD, 2, uriInfo)).thenReturn(nextUrl);

        final Position position = position(3L);
        when(positionFactory.createPosition("3")).thenReturn(position);
        when(service.events(streamId, position, FORWARD, 2L)).thenReturn(events);

        final Page<EventEntry> feedActual = eventsPageService.pageEvents(streamId, "3", FORWARD, 2, uriInfo);

        final List<EventEntry> feed = feedActual.getData();

        final PagingLinks pagingLinks = feedActual.getPagingLinks();

        assertThat(feed, hasSize(2));

        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(0).getPosition(), is(2L));
        assertThat(feed.get(0).getPayload(), is(payloadEvent2));
        assertThat(feed.get(1).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(1).getPosition(), is(1L));
        assertThat(feed.get(1).getPayload(), is(payloadEvent1));

        assertThat(pagingLinks.getNext().get(), is(nextUrl));
        assertThat(pagingLinks.getPrevious(), is(empty()));
        assertThat(pagingLinks.getHead(), is(headURL));
        assertThat(pagingLinks.getFirst(), is(firstURL));
    }


    @Test
    public void shouldReturnResultWhenSameNumberOfRecordsAsPageSizeWhenLookingForOlderEvents() throws Exception {
        final UUID streamId = randomUUID();

        final List<EventEntry> events = new ArrayList<>();

        final JsonObject payloadEvent4 = createObjectBuilder().add("field4", "value4").build();
        final JsonObject payloadEvent3 = createObjectBuilder().add("field3", "value3").build();

        final EventEntry event4 = new EventEntry(randomUUID(), streamId, 4L, "Test Name4", payloadEvent4, new UtcClock().now().toString());
        final EventEntry event3 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", payloadEvent3, new UtcClock().now().toString());

        events.add(event4);
        events.add(event3);

        when(service.eventExists(streamId, 5L)).thenReturn(false);

        when(service.eventExists(streamId, 2L)).thenReturn(true);

        final Position position = position(4L);
        when(positionFactory.createPosition("4")).thenReturn(position);
        when(service.events(streamId, position, BACKWARD, 2)).thenReturn(events);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL previousUrl = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/2/BACKWARD/2");
        when(urlLinkFactory.createEventsUrlLink(position(2L), BACKWARD, 2, uriInfo)).thenReturn(previousUrl);

        final Page<EventEntry> feedActual = eventsPageService.pageEvents(streamId, "4", BACKWARD, 2, uriInfo);

        final List<EventEntry> feed = feedActual.getData();

        final PagingLinks pagingLinks = feedActual.getPagingLinks();

        assertThat(feed, hasSize(2));
        assertThat(feed.get(0).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(0).getPosition(), is(4L));
        assertThat(feed.get(0).getPayload(), is(payloadEvent4));
        assertThat(feed.get(1).getStreamId(), is(streamId.toString()));
        assertThat(feed.get(1).getPosition(), is(3L));
        assertThat(feed.get(1).getPayload(), is(payloadEvent3));

        assertThat(pagingLinks.getNext(), is(empty()));
        assertThat(pagingLinks.getPrevious().get(), is(previousUrl));

        assertThat(pagingLinks.getHead(), is(headURL));
        assertThat(pagingLinks.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnLinkForPage2IfOnLast() throws Exception {

        final UUID streamId = randomUUID();

        final List<EventEntry> events = new ArrayList<>();

        final EventEntry event1 = new EventEntry(randomUUID(), streamId, 1L, "Test Name1", createObjectBuilder().add("field1", "value1").build(), new UtcClock().now().toString());
        final EventEntry event2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", createObjectBuilder().add("field2", "value2").build(), new UtcClock().now().toString());

        events.add(event2);

        events.add(event1);

        when(service.eventExists(streamId, 3L)).thenReturn(true);

        when(positionFactory.createPosition("1")).thenReturn(first());
        when(service.events(streamId, first(), FORWARD, 2L)).thenReturn(events);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL nextUrl = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/3/FORWARD/2");
        when(urlLinkFactory.createEventsUrlLink(position(3L), FORWARD, 2, uriInfo)).thenReturn(nextUrl);

        final Page<EventEntry> feed = eventsPageService.pageEvents(streamId, FIRST, FORWARD, 2, uriInfo);
        final PagingLinks pagingLinks = feed.getPagingLinks();

        assertThat(pagingLinks.getNext().get(), is(nextUrl));
        assertThat(pagingLinks.getPrevious(), is(empty()));
        assertThat(pagingLinks.getHead(), is(headURL));
        assertThat(pagingLinks.getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnLinkForPage2OnHead() throws Exception {

        final UUID streamId = randomUUID();

        final List<EventEntry> events = new ArrayList<>();

        final EventEntry event2 = new EventEntry(randomUUID(), streamId, 2L, "Test Name2", createObjectBuilder().add("field2", "value2").build(), new UtcClock().now().toString());
        final EventEntry event3 = new EventEntry(randomUUID(), streamId, 3L, "Test Name3", createObjectBuilder().add("field3", "value3").build(), new UtcClock().now().toString());

        events.add(event3);
        events.add(event2);

        when(service.eventExists(streamId, 1L)).thenReturn(true);

        when(positionFactory.createPosition(HEAD)).thenReturn(head());
        when(service.events(streamId, head(), BACKWARD, 2L)).thenReturn(events);

        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"), create("event-streams/" + streamId));

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final URL previousURL = new URL(BASE_URL + EVENT_STREAM_PATH + "/" + streamId + "1/FORWARD/2");
        when(urlLinkFactory.createEventsUrlLink(position(1L), BACKWARD, 2, uriInfo)).thenReturn(previousURL);

        final Page<EventEntry> feed = eventsPageService.pageEvents(streamId, HEAD, BACKWARD, 2, uriInfo);

        assertTrue(feed.getPagingLinks().getNext().equals(empty()));
        assertThat(feed.getPagingLinks().getPrevious().get(), is(previousURL));
        assertThat(feed.getPagingLinks().getHead(), is(headURL));
        assertThat(feed.getPagingLinks().getFirst(), is(firstURL));
    }

    @Test
    public void shouldReturnEmptyListWithPagingLinks() throws Exception {
        final UUID streamId = randomUUID();
        final Position sequence = position(3L);
        final Direction backward = BACKWARD;
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo(create("http://server:123/context/"),
                create("event-st¬reams/" + streamId));

        final List<EventEntry> entries = emptyList();

        final URL headURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/HEAD/BACKWARD/2");
        when(urlLinkFactory.createHeadEventsUrlLink(2, uriInfo)).thenReturn(headURL);

        final URL firstURL = new URL(BASE_URL + EVENT_STREAM_PATH + streamId + "/1/FORWARD/2");
        when(urlLinkFactory.createFirstEventsUrlLink(2, uriInfo)).thenReturn(firstURL);

        final Page<EventEntry> eventEntryPage = eventsPageService.pageEvents(streamId, "3", backward, 2, uriInfo);
        assertThat(eventEntryPage.getData(), is(entries));
        assertThat(eventEntryPage.getPagingLinks().getFirst(), is(firstURL));
        assertThat(eventEntryPage.getPagingLinks().getHead(), is(headURL));
        assertThat(eventEntryPage.getPagingLinks().getNext(), is(empty()));
        assertThat(eventEntryPage.getPagingLinks().getPrevious(), is(empty()));

    }
}
