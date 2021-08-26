package uk.gov.justice.services.eventsourcing.source.api;

import static com.jayway.jsonassert.JsonAssert.with;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.openejb.util.NetworkUtil.getNextAvailablePort;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.core.postgres.OpenEjbConfigurationBuilder.createOpenEjbConfigurationBuilder;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.BACKWARD;
import static uk.gov.justice.services.eventsourcing.source.api.service.core.Direction.FORWARD;
import static uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider.SYSTEM_USER_ID;

import uk.gov.justice.services.cdi.InitialContextProducer;
import uk.gov.justice.services.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.common.configuration.ContextNameProvider;
import uk.gov.justice.services.common.configuration.GlobalValueProducer;
import uk.gov.justice.services.common.configuration.JndiBasedServiceContextNameProvider;
import uk.gov.justice.services.common.converter.JsonObjectConvertersProducer;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.rest.BadRequestExceptionMapper;
import uk.gov.justice.services.common.rest.ForbiddenRequestExceptionMapper;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.enveloper.DefaultEnveloper;
import uk.gov.justice.services.core.json.DefaultJsonValidationLoggerHelper;
import uk.gov.justice.services.core.json.JsonValidationLoggerHelper;
import uk.gov.justice.services.eventsource.DefaultEventDestinationResolver;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.publisher.jms.JmsEventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.AnsiSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.JdbcBasedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.PostgresSQLEventLogInsertionStrategy;
import uk.gov.justice.services.eventsourcing.repository.jdbc.PrePublishQueueRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.PublishQueueRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.PublishQueuesDataAccess;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.MultipleDataSourcePublishedEventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventSourceApiApplication;
import uk.gov.justice.services.eventsourcing.source.api.resource.EventStreamPageResource;
import uk.gov.justice.services.eventsourcing.source.api.security.AccessController;
import uk.gov.justice.services.eventsourcing.source.api.service.EventStreamPageService;
import uk.gov.justice.services.eventsourcing.source.api.service.UrlLinkFactory;
import uk.gov.justice.services.eventsourcing.source.api.service.core.Direction;
import uk.gov.justice.services.eventsourcing.source.api.service.core.EventStreamService;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionFactory;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PositionValueFactory;
import uk.gov.justice.services.eventsourcing.source.api.util.LoggerProducer;
import uk.gov.justice.services.eventsourcing.source.api.util.TestSystemUserProvider;
import uk.gov.justice.services.eventsourcing.source.core.EventAppender;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventSourceNameProvider;
import uk.gov.justice.services.eventsourcing.source.core.EventSourceProducer;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventsourcing.source.core.EventStreamManager;
import uk.gov.justice.services.eventsourcing.source.core.JdbcBasedEventSource;
import uk.gov.justice.services.eventsourcing.source.core.JdbcEventSourceFactory;
import uk.gov.justice.services.eventsourcing.source.core.MaxRetryProvider;
import uk.gov.justice.services.eventsourcing.source.core.PublishingEventAppender;
import uk.gov.justice.services.eventsourcing.source.core.SystemEventService;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.JndiAppNameProvider;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;
import uk.gov.justice.services.messaging.logging.DefaultTraceLogger;
import uk.gov.justice.services.messaging.logging.TraceLogger;
import uk.gov.justice.services.messaging.spi.DefaultJsonEnvelopeProvider;
import uk.gov.justice.services.test.utils.messaging.jms.DummyJmsEnvelopeSender;
import uk.gov.justice.services.test.utils.persistence.OpenEjbEventStoreDataSourceProvider;
import uk.gov.justice.services.yaml.YamlParser;
import uk.gov.justice.services.yaml.YamlSchemaLoader;
import uk.gov.justice.subscription.ParserProducer;
import uk.gov.justice.subscription.SubscriptionSorter;
import uk.gov.justice.subscription.YamlFileFinder;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistryProducer;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.jayway.jsonpath.JsonPath;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class EventStreamPageIT {

    private static final String LIQUIBASE_EVENT_STORE_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private static final String BASE_URI_PATTERN = "http://localhost:%d/event-source-api/rest";
    private static final String EVENT_STREAM_URL_PATH_PREFIX = "/event-source-api/rest/event-streams";
    private static final int PAGE_SIZE = 2;
    private static int port = -1;

    private CloseableHttpClient httpClient;

    @Inject
    private EventStreamJdbcRepository eventsRepository;

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @BeforeClass
    public static void beforeClass() {
        port = getNextAvailablePort();
    }

    @Before
    public void setup() throws Exception {
        httpClient = HttpClients.createDefault();
        initEventDatabase();
    }

    @Configuration
    public Properties postgresqlConfiguration() {
        return createOpenEjbConfigurationBuilder()
                .addInitialContext()
                .addHttpEjbPort(port)
                .addPostgresqlViewStore()
                .build();
    }

    @ApplicationScoped
    public static class TestEventInsertionStrategyProducer {

        @Produces
        public EventInsertionStrategy eventLogInsertionStrategy() {
            return new PostgresSQLEventLogInsertionStrategy();
        }
    }

    @Module
    @Classes(cdi = true, value = {
            ObjectMapperProducer.class,
            ObjectToJsonValueConverter.class,
            EventStreamPageResource.class,
            EventStreamService.class,
            AccessController.class,
            AnsiSQLEventLogInsertionStrategy.class,
            TestSystemUserProvider.class,
            ForbiddenRequestExceptionMapper.class,
            TestEventInsertionStrategyProducer.class,
            EventStreamPageService.class,
            LoggerProducer.class,
            PositionFactory.class,
            UrlLinkFactory.class,
            PositionValueFactory.class,
            JdbcResultSetStreamer.class,
            PreparedStatementWrapperFactory.class,
            UtcClock.class,
            JsonValidationLoggerHelper.class,
            BadRequestExceptionMapper.class,
            DefaultJsonValidationLoggerHelper.class,
            EventSource.class,
            JdbcBasedEventSource.class,
            EventAppender.class,
            PublishingEventAppender.class,
            EventConverter.class,
            SystemEventService.class,
            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            GlobalValueProducer.class,
            DefaultEnveloper.class,
            EventPublisher.class,
            JmsEventPublisher.class,
            DefaultEventDestinationResolver.class,
            DefaultJsonObjectEnvelopeConverter.class,
            DummyJmsEnvelopeSender.class,
            TraceLogger.class,
            DefaultTraceLogger.class,
            EventSourceProducer.class,
            EventSourceDefinitionRegistryProducer.class,
            ParserProducer.class,
            YamlFileFinder.class,
            YamlParser.class,
            YamlSchemaLoader.class,
            JmsEnvelopeSender.class,
            DefaultEnvelopeConverter.class,
            OpenEjbEventStoreDataSourceProvider.class,

            InitialContextProducer.class,

            QualifierAnnotationExtractor.class,
            JdbcEventSourceFactory.class,

            DefaultEventSourceDefinitionFactory.class,
            SubscriptionSorter.class,
            MultipleDataSourcePublishedEventRepository.class,
            EventJdbcRepository.class,
            EventStreamJdbcRepository.class,
            JdbcBasedEventRepository.class,
            MaxRetryProvider.class,
            EventSourceNameProvider.class,
            EventStreamManager.class,

            JndiAppNameProvider.class,
            DefaultJsonEnvelopeProvider.class,
            JsonObjectConvertersProducer.class,

            ContextNameProvider.class,
            JndiBasedServiceContextNameProvider.class,
            PublishQueuesDataAccess.class,
            PrePublishQueueRepository.class,
            PublishQueueRepository.class
    })

    public WebApp war() {
        return new WebApp()
                .contextRoot("event-source-api")
                .addServlet("EventStreamPageITApp", Application.class.getName())
                .addInitParam("EventStreamPageITApp", "javax.ws.rs.Application", EventSourceApiApplication.class.getName());
    }

    @Test
    public void shouldReturnTheFullEventInfo() throws Exception {
        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "3", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String value = responseBodyOf(response);

        final String selfLinkForStreamId3 = "/event-source-api/rest/event-streams/" + streamId3 + "/HEAD/BACKWARD/2";
        final String selfLinkForStreamId2 = "/event-source-api/rest/event-streams/" + streamId2 + "/HEAD/BACKWARD/2";

        with(value)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].self", containsString(selfLinkForStreamId2))
                .assertThat("$.data[0].sequenceNumber", is(2))

                .assertThat("$.data[1].self", containsString(selfLinkForStreamId3))
                .assertThat("$.data[1].sequenceNumber", is(3));
    }

    @Test
    public void shouldReturnHeadEvents() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "HEAD", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page1 = responseBodyOf(response);
        final String expectedSelfLinkForStreamId5 = "/event-source-api/rest/event-streams/" + streamId5 + "/HEAD/BACKWARD/2";
        final String expectedSelfLinkForStreamId4 = "/event-source-api/rest/event-streams/" + streamId4 + "/HEAD/BACKWARD/2";

        with(page1)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].self", containsString(expectedSelfLinkForStreamId4))
                .assertThat("$.data[0].sequenceNumber", is(4))

                .assertThat("$.data[1].self", containsString(expectedSelfLinkForStreamId5))
                .assertThat("$.data[1].sequenceNumber", is(5));

        with(page1)
                .assertNotDefined("$.pagingLinks.next");

        final String page2Url = JsonPath.read(page1, "$.pagingLinks.previous");

        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page1);
    }

    @Test
    public void shouldThrowExceptionWhenHeadRequestednextAsDirection() throws Exception {

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "HEAD", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturnLatestEvents() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "5", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page1 = responseBodyOf(response);

        final String selfLinkForStreamId5 = "/event-source-api/rest/event-streams/" + streamId5 + "/HEAD/BACKWARD/2";
        final String selfLinkForStreamId4 = "/event-source-api/rest/event-streams/" + streamId4 + "/HEAD/BACKWARD/2";

        with(page1)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].self", containsString(selfLinkForStreamId4))
                .assertThat("$.data[0].sequenceNumber", is(4))

                .assertThat("$.data[1].self", containsString(selfLinkForStreamId5))
                .assertThat("$.data[1].sequenceNumber", is(5));

        with(page1)
                .assertNotDefined("$.pagingLinks.next");

        assertHeadAndLastLinks(page1);

        final String page2Url = JsonPath.read(page1, "$.pagingLinks.previous");

        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));
    }


    @Test
    public void shouldReturnOldestEvents() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "1", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page3 = responseBodyOf(response);

        final String selfLinkForStreamId2 = "/event-source-api/rest/event-streams/" + streamId2 + "/HEAD/BACKWARD/2";
        final String selfLinkForStreamId1 = "/event-source-api/rest/event-streams/" + streamId1 + "/HEAD/BACKWARD/2";

        with(page3)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].self", containsString(selfLinkForStreamId1))
                .assertThat("$.data[0].sequenceNumber", is(1))

                .assertThat("$.data[1].self", containsString(selfLinkForStreamId2))
                .assertThat("$.data[1].sequenceNumber", is(2));

        assertHeadAndLastLinks(page3);

        final String page2Url = JsonPath.read(page3, "$.pagingLinks.next");
        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/FORWARD/" + PAGE_SIZE));

        with(page3)
                .assertNotDefined("$.pagingLinks.previous");
    }

    @Test
    public void shouldThrowExceptionWhenFirstRequestedpreviousAsDirection() throws Exception {

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "1", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(BAD_REQUEST.getStatusCode()));

    }

    @Test
    public void shouldReturnOlderEventspreviousAndNextLinks() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "3", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page2 = responseBodyOf(response);

        final String selfLinkForStreamId3 = "/event-source-api/rest/event-streams/" + streamId3 + "/HEAD/BACKWARD/2";
        final String selfLinkForStreamId2 = "/event-source-api/rest/event-streams/" + streamId2 + "/HEAD/BACKWARD/2";

        with(page2)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].self", containsString(selfLinkForStreamId2))
                .assertThat("$.data[0].sequenceNumber", is(2))

                .assertThat("$.data[1].self", containsString(selfLinkForStreamId3))
                .assertThat("$.data[1].sequenceNumber", is(3));

        assertHeadAndLastLinks(page2);

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.next");

        assertThat(page3Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/4/FORWARD/" + PAGE_SIZE));

        final String page1Url = JsonPath.read(page2, "$.pagingLinks.previous");

        assertThat(page1Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));
    }


    @Test
    public void shouldReturnNewerEventspreviousAndNextLinks() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "3", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String page2 = responseBodyOf(response);

        final String selfLinkForStreamId4 = "/event-source-api/rest/event-streams/" + streamId4 + "/HEAD/BACKWARD/2";

        final String selfLinkForStreamId3 = "/event-source-api/rest/event-streams/" + streamId3 + "/HEAD/BACKWARD/2";

        with(page2)
                .assertThat("$.data", hasSize(2))

                .assertThat("$.data[0].self", containsString(selfLinkForStreamId3))
                .assertThat("$.data[0].sequenceNumber", is(3))

                .assertThat("$.data[1].self", containsString(selfLinkForStreamId4))
                .assertThat("$.data[1].sequenceNumber", is(4));

        final String page1Url = JsonPath.read(page2, "$.pagingLinks.next");

        assertThat(page1Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/5/FORWARD/" + PAGE_SIZE));

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.previous");

        assertThat(page3Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/2/BACKWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page2);
    }


    @Test
    public void shouldNotReturnRecordsForUnknownSequenceId() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "6", FORWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(0));

        assertHeadAndLastLinks(value);
    }

    @Test
    public void shouldReturnEmptyFeedIfNoDataAndNoPreviousAndNextLinks() throws IOException {

        eventsRepository.findAll();
        final HttpResponse response = eventsStreamsFor(SYSTEM_USER_ID, "5", BACKWARD, PAGE_SIZE);

        assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        String value = responseBodyOf(response);
        with(value)
                .assertThat("$.data", hasSize(0));

        with(value)
                .assertNotDefined("$.pagingLinks.previous");

        with(value)
                .assertNotDefined("$.pagingLinks.next");


        assertHeadAndLastLinks(value);
    }

    @Test
    public void shouldGoToPage2FromHead() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse firstPageResponse = eventsStreamsFor(SYSTEM_USER_ID, "HEAD", BACKWARD, PAGE_SIZE);

        assertThat(firstPageResponse.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

        final String firstPage = responseBodyOf(firstPageResponse);

        with(firstPage)
                .assertNotDefined("$.pagingLinks.next");

        final String page2Url = JsonPath.read(firstPage, "$.pagingLinks.previous");

        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));

        final HttpResponse page2HttpResponse = feedOf(page2Url, SYSTEM_USER_ID);

        final String page2 = responseBodyOf(page2HttpResponse);

        final String selfLinkForStreamId3 = "/event-source-api/rest/event-streams/" + streamId3 + "/HEAD/BACKWARD/2";

        final String selfLinkForStreamId2 = "/event-source-api/rest/event-streams/" + streamId2 + "/HEAD/BACKWARD/2";

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].self", containsString(selfLinkForStreamId2))
                .assertThat("$.data[0].sequenceNumber", is(2))
                .assertThat("$.data[1].self", containsString(selfLinkForStreamId3))
                .assertThat("$.data[1].sequenceNumber", is(3));

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.next");
        assertThat(page3Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/4/FORWARD/" + PAGE_SIZE));

        final String page1Url = JsonPath.read(page2, "$.pagingLinks.previous");
        assertThat(page1Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page2);
    }


    @Test
    public void shouldGoToPage3FromPage2() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse secondPageResponse = eventsStreamsFor(SYSTEM_USER_ID, "3", BACKWARD, PAGE_SIZE);

        final String page2 = responseBodyOf(secondPageResponse);

        final String selfLinkForStreamId3 = "/event-source-api/rest/event-streams/" + streamId3 + "/HEAD/BACKWARD/2";

        final String selfLinkForStreamId2 = "/event-source-api/rest/event-streams/" + streamId2 + "/HEAD/BACKWARD/2";

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].self", containsString(selfLinkForStreamId2))
                .assertThat("$.data[0].sequenceNumber", is(2))
                .assertThat("$.data[1].self", containsString(selfLinkForStreamId3))
                .assertThat("$.data[1].sequenceNumber", is(3));

        final String page3Url = JsonPath.read(page2, "$.pagingLinks.previous");

        final HttpResponse page3HttpResponse = feedOf(page3Url, SYSTEM_USER_ID);

        final String page3 = responseBodyOf(page3HttpResponse);

        final String selfLinkForStreamId1 = "/event-source-api/rest/event-streams/" + streamId1 + "/HEAD/BACKWARD/2";

        with(page3)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].self", containsString(selfLinkForStreamId1))
                .assertThat("$.data[0].sequenceNumber", is(1))
                .assertThat("$.data[1].self", containsString(selfLinkForStreamId2))
                .assertThat("$.data[1].sequenceNumber", is(2));

        with(page3)
                .assertNotDefined("$.pagingLinks.previous");
        final String page2Url = JsonPath.read(page3, "$.pagingLinks.next");
        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/FORWARD/" + PAGE_SIZE));

        assertHeadAndLastLinks(page3);
    }

    @Test
    public void shouldGotoPage1FromPage2() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        final UUID streamId4 = randomUUID();
        final UUID streamId5 = randomUUID();

        storeEventStreams(streamId1, streamId2, streamId3, streamId4, streamId5);

        final HttpResponse secondPageResponse = eventsStreamsFor(SYSTEM_USER_ID, "3", BACKWARD, PAGE_SIZE);

        final String page2 = responseBodyOf(secondPageResponse);

        final String selfLinkForStreamId3 = "/event-source-api/rest/event-streams/" + streamId3 + "/HEAD/BACKWARD/2";
        final String selfLinkForStreamId2 = "/event-source-api/rest/event-streams/" + streamId2 + "/HEAD/BACKWARD/2";

        with(page2)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].self", containsString(selfLinkForStreamId2))
                .assertThat("$.data[0].sequenceNumber", is(2))
                .assertThat("$.data[1].self", containsString(selfLinkForStreamId3))
                .assertThat("$.data[1].sequenceNumber", is(3));

        final String page1Url = JsonPath.read(page2, "$.pagingLinks.next");

        final HttpResponse page1HttpResponse = feedOf(page1Url, SYSTEM_USER_ID);

        final String page1 = responseBodyOf(page1HttpResponse);

        final String selfLinkForStreamId5 = "/event-source-api/rest/event-streams/" + streamId5 + "/HEAD/BACKWARD/2";
        final String selfLinkForStreamId4 = "/event-source-api/rest/event-streams/" + streamId4 + "/HEAD/BACKWARD/2";

        with(page1)
                .assertThat("$.data", hasSize(2))
                .assertThat("$.data[0].self", containsString(selfLinkForStreamId4))
                .assertThat("$.data[0].sequenceNumber", is(4))
                .assertThat("$.data[1].self", containsString(selfLinkForStreamId5))
                .assertThat("$.data[1].sequenceNumber", is(5));

        assertHeadAndLastLinks(page1);

        with(page1)
                .assertNotDefined("$.pagingLinks.next");

        final String page2Url = JsonPath.read(page1, "$.pagingLinks.previous");
        assertThat(page2Url, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/3/BACKWARD/" + PAGE_SIZE));
    }

    @Test
    public void shouldReturnForbiddenIfNotASystemUser() throws IOException {
        final HttpResponse response = eventsStreamsFor(randomUUID(), "12", BACKWARD, PAGE_SIZE);
        assertThat(response.getStatusLine().getStatusCode(), is(FORBIDDEN.getStatusCode()));
    }

    private String responseBodyOf(final HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private HttpResponse eventsStreamsFor(final UUID userId,
                                          final String position,
                                          final Direction link,
                                          final long pageSize) throws IOException {
        final String url = format(BASE_URI_PATTERN + "/event-streams/" + position + "/" + link + "/" + pageSize, port);
        return feedOf(url, userId);
    }

    private HttpResponse feedOf(final String url, final UUID userId) throws IOException {
        final HttpUriRequest request = new HttpGet(url);
        request.addHeader(USER_ID, userId.toString());
        return httpClient.execute(request);
    }

    private void initEventDatabase() throws Exception {

        Liquibase eventStoreLiquibase = new Liquibase(LIQUIBASE_EVENT_STORE_CHANGELOG_XML,
                new ClassLoaderResourceAccessor(), new JdbcConnection(eventStoreDataSourceProvider.getDefaultDataSource().getConnection()));
        eventStoreLiquibase.dropAll();
        eventStoreLiquibase.update("");
    }

    private void storeEventStreams(final UUID streamId1,
                                   final UUID streamId2,
                                   final UUID streamId3,
                                   final UUID streamId4,
                                   final UUID streamId5) {

        eventsRepository.insert(streamId1);
        eventsRepository.insert(streamId2);
        eventsRepository.insert(streamId3);
        eventsRepository.insert(streamId4);
        eventsRepository.insert(streamId5);
    }

    private void assertHeadAndLastLinks(String value) {
        final String headUrl = JsonPath.read(value, "$.pagingLinks.head");
        assertThat(headUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/HEAD/BACKWARD/" + PAGE_SIZE));

        final String firstUrl = JsonPath.read(value, "$.pagingLinks.first");
        assertThat(firstUrl, containsString(EVENT_STREAM_URL_PATH_PREFIX + "/1/FORWARD/" + PAGE_SIZE));
    }
}
