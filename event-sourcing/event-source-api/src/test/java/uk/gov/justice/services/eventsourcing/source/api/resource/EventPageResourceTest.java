package uk.gov.justice.services.eventsourcing.source.api.resource;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.source.api.service.PagingLinks.PagingLinksBuilder.pagingLinksBuilder;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.FIRST;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.FixedPositionValue.HEAD;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventsPageService;
import uk.gov.justice.services.eventsourcing.source.api.service.Page;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Direction;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventEntry;

import java.net.URL;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventPageResourceTest {

    @Mock
    private EventsPageService eventsPageService;

    @Mock
    private AccessController accessControlChecker;

    @Mock
    private ObjectToJsonValueConverter converter;

    @InjectMocks
    private EventPageResource resource;

    @Test
    public void shouldReturnFeedReturnedByService() throws Exception {
        final UUID streamId = randomUUID();
        final String position = "2";
        final String positionValue = "2";
        final int pageSize = 10;

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page<EventEntry> page = new Page<>(emptyList(), pagingLinksBuilder(fixedUrl, fixedUrl).build());

        when(eventsPageService.pageEvents(streamId, position, FORWARD, pageSize, uriInfo)).thenReturn(page);

        final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("key", "value");

        when(converter.convert(page)).thenReturn(jsonObjectBuilder.build());

        resource.events(streamId.toString(), positionValue, FORWARD.toString(), pageSize, uriInfo);

        final ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<String> positionCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<UriInfo> uriInfoCaptor = ArgumentCaptor.forClass(UriInfo.class);
        final ArgumentCaptor<Direction> directionCaptor = ArgumentCaptor.forClass(Direction.class);

        verify(eventsPageService).pageEvents(uuidArgumentCaptor.capture(), positionCaptor.capture(), directionCaptor.capture(), pageSizeCaptor.capture(), uriInfoCaptor.capture());

        assertThat(positionCaptor.getValue(), is(position));

        assertThat(pageSizeCaptor.getValue(), is(pageSize));

        assertThat(uriInfoCaptor.getValue(), is(uriInfo));

        assertThat(directionCaptor.getValue(), is(FORWARD));

        assertThat(uuidArgumentCaptor.getValue(), is(streamId));
    }

    @Test
    public void shouldReturnBadRequestWhenHeadEventsRequestedWithForwardDirection() throws Exception {

        final String streamId = randomUUID().toString();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page<EventEntry> page = new Page<>(emptyList(), pagingLinksBuilder(fixedUrl, fixedUrl).build());

        assertThrows(BadRequestException.class, () -> resource.events(streamId, HEAD, FORWARD.toString(), 10, uriInfo));
    }

    @Test
    public void shouldReturnBadRequestWhenFirstEventsRequestedWithBackwardDirection() throws Exception {

        final String streamId = randomUUID().toString();

        final UriInfo uriInfo = new ResteasyUriInfo("" + "/" + streamId, "", "");

        final URL fixedUrl = new URL("http://localhost:8080/rest/fixed");

        final Page<EventEntry> page = new Page<>(emptyList(), pagingLinksBuilder(fixedUrl, fixedUrl).build());

        assertThrows(BadRequestException.class, () -> resource.events(streamId, FIRST, BACKWARD.toString(), 10, uriInfo));
    }

    @Test
    public void shouldCheckAccessRights() throws Exception {

        final String streamId = randomUUID().toString();

        final ResteasyHttpHeaders requestHeaders = new ResteasyHttpHeaders(new MultivaluedHashMap<>());

        resource.headers = requestHeaders;

        resource.events(streamId, "3", FORWARD.toString(), 1, new ResteasyUriInfo("", "", ""));

        verify(accessControlChecker).checkAccessControl(requestHeaders);
    }
}
