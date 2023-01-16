package uk.gov.justice.services.subscription;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessedEventTrackingRepositoryIT {

    private final DataSource viewStoreDataSource = new FrameworkTestDataSourceFactory().createViewStoreDataSource();

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @SuppressWarnings("unused")
    @Spy
    private JdbcResultSetStreamer jdbcResultSetStreamer = new JdbcResultSetStreamer();

    @SuppressWarnings("unused")
    @Spy
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();

    @InjectMocks
    private ProcessedEventTrackingRepository processedEventTrackingRepository;

    @Before
    public void ensureOurDatasourceProviderReturnsOurTestDataSource() {

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);
    }

    @Before
    public void cleanTable() {
        new DatabaseCleaner().cleanViewStoreTables("framework", "processed_event");
    }

    @Test
    public void shouldSaveAndGetAllProcessedEvents() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final ProcessedEvent processedEvent_1 = new ProcessedEvent(randomUUID(), 0, 1, source, componentName);
        final ProcessedEvent processedEvent_2 = new ProcessedEvent(randomUUID(), 1, 2, source, componentName);
        final ProcessedEvent processedEvent_3 = new ProcessedEvent(randomUUID(), 2, 3, source, componentName);
        final ProcessedEvent processedEvent_4 = new ProcessedEvent(randomUUID(), 3, 4, source, componentName);

        processedEventTrackingRepository.save(processedEvent_1);
        processedEventTrackingRepository.save(processedEvent_2);
        processedEventTrackingRepository.save(processedEvent_3);
        processedEventTrackingRepository.save(processedEvent_4);

        final Stream<ProcessedEvent> allProcessedEvents = processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName);

        final List<ProcessedEvent> processedEvents = allProcessedEvents.collect(toList());

        assertThat(processedEvents.size(), is(4));

        assertThat(processedEvents.get(0), is(processedEvent_4));
        assertThat(processedEvents.get(1), is(processedEvent_3));
        assertThat(processedEvents.get(2), is(processedEvent_2));
        assertThat(processedEvents.get(3), is(processedEvent_1));
    }

    @Test
    public void shouldReturnProcessedEventsInDescendingOrderIfInsertedOutOfOrder() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final ProcessedEvent processedEvent_1 = new ProcessedEvent(randomUUID(), 0, 1, source, componentName);
        final ProcessedEvent processedEvent_2 = new ProcessedEvent(randomUUID(), 1, 2, source, componentName);
        final ProcessedEvent processedEvent_3 = new ProcessedEvent(randomUUID(), 2, 3, source, componentName);
        final ProcessedEvent processedEvent_4 = new ProcessedEvent(randomUUID(), 3, 4, source, componentName);

        processedEventTrackingRepository.save(processedEvent_2);
        processedEventTrackingRepository.save(processedEvent_4);
        processedEventTrackingRepository.save(processedEvent_1);
        processedEventTrackingRepository.save(processedEvent_3);

        final Stream<ProcessedEvent> allProcessedEvents = processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName);

        final List<ProcessedEvent> processedEvents = allProcessedEvents.collect(toList());

        assertThat(processedEvents.size(), is(4));

        assertThat(processedEvents.get(0), is(processedEvent_4));
        assertThat(processedEvents.get(1), is(processedEvent_3));
        assertThat(processedEvents.get(2), is(processedEvent_2));
        assertThat(processedEvents.get(3), is(processedEvent_1));
    }

    @Test
    public void shouldReturnOnlyProcessedEventsWIthTheCorrectSourceInDescendingOrder() throws Exception {

        final String source = "example-context";
        final String otherSource = "another-context";
        final String componentName = "EVENT_LISTENER";

        final ProcessedEvent processedEvent_1 = new ProcessedEvent(randomUUID(), 0, 1, source, componentName);
        final ProcessedEvent processedEvent_2 = new ProcessedEvent(randomUUID(), 1, 2, source, componentName);
        final ProcessedEvent processedEvent_3 = new ProcessedEvent(randomUUID(), 2, 3, source, componentName);
        final ProcessedEvent processedEvent_4 = new ProcessedEvent(randomUUID(), 3, 4, source, componentName);

        final ProcessedEvent processedEvent_5 = new ProcessedEvent(randomUUID(), 0, 1, otherSource, componentName);
        final ProcessedEvent processedEvent_6 = new ProcessedEvent(randomUUID(), 1, 2, otherSource, componentName);
        final ProcessedEvent processedEvent_7 = new ProcessedEvent(randomUUID(), 2, 3, otherSource, componentName);
        final ProcessedEvent processedEvent_8 = new ProcessedEvent(randomUUID(), 3, 4, otherSource, componentName);
        final ProcessedEvent processedEvent_9 = new ProcessedEvent(randomUUID(), 5, 6, otherSource, componentName);

        processedEventTrackingRepository.save(processedEvent_2);
        processedEventTrackingRepository.save(processedEvent_4);
        processedEventTrackingRepository.save(processedEvent_1);
        processedEventTrackingRepository.save(processedEvent_6);
        processedEventTrackingRepository.save(processedEvent_7);
        processedEventTrackingRepository.save(processedEvent_5);
        processedEventTrackingRepository.save(processedEvent_8);
        processedEventTrackingRepository.save(processedEvent_3);
        processedEventTrackingRepository.save(processedEvent_9);

        final Stream<ProcessedEvent> allProcessedEvents = processedEventTrackingRepository.getAllProcessedEventsDescendingOrder(source, componentName);

        final List<ProcessedEvent> processedEvents = allProcessedEvents.collect(toList());

        assertThat(processedEvents.size(), is(4));

        assertThat(processedEvents.get(0), is(processedEvent_4));
        assertThat(processedEvents.get(1), is(processedEvent_3));
        assertThat(processedEvents.get(2), is(processedEvent_2));
        assertThat(processedEvents.get(3), is(processedEvent_1));
    }

    @Test
    public void shouldGetTheLatestProcessedEvent() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        final ProcessedEvent processedEvent_1 = new ProcessedEvent(randomUUID(), 0, 1, source, componentName);
        final ProcessedEvent processedEvent_2 = new ProcessedEvent(randomUUID(), 1, 2, source, componentName);
        final ProcessedEvent processedEvent_3 = new ProcessedEvent(randomUUID(), 2, 3, source, componentName);
        final ProcessedEvent processedEvent_4 = new ProcessedEvent(randomUUID(), 3, 4, source, componentName);
        final ProcessedEvent processedEvent_5 = new ProcessedEvent(randomUUID(), 99, 100, "a-different-context", componentName);

        processedEventTrackingRepository.save(processedEvent_2);
        processedEventTrackingRepository.save(processedEvent_5);
        processedEventTrackingRepository.save(processedEvent_4);
        processedEventTrackingRepository.save(processedEvent_1);
        processedEventTrackingRepository.save(processedEvent_3);

        final Optional<ProcessedEvent> latestProcessedEvent = processedEventTrackingRepository.getLatestProcessedEvent(source, componentName);

        if (latestProcessedEvent.isPresent()) {

            final ProcessedEvent processedEvent = latestProcessedEvent.get();

            assertThat(processedEvent.getEventNumber(), is(4L));
            assertThat(processedEvent.getPreviousEventNumber(), is(3L));
            assertThat(processedEvent.getSource(), is(source));

        } else {
            fail();
        }
    }
}
