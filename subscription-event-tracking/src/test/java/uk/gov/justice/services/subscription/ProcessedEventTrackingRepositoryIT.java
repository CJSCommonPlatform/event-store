package uk.gov.justice.services.subscription;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
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
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ProcessedEventTrackingRepositoryIT {

    private final DataSource viewStoreDataSource = new FrameworkTestDataSourceFactory().createViewStoreDataSource();

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @SuppressWarnings("unused")
    @Spy
    private JdbcRepositoryHelper jdbcRepositoryHelper = new JdbcRepositoryHelper();

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

        final ProcessedEventTrackItem processedEventTrackItem_1 = new ProcessedEventTrackItem(0, 1, source);
        final ProcessedEventTrackItem processedEventTrackItem_2 = new ProcessedEventTrackItem(1, 2, source);
        final ProcessedEventTrackItem processedEventTrackItem_3 = new ProcessedEventTrackItem(2, 3, source);
        final ProcessedEventTrackItem processedEventTrackItem_4 = new ProcessedEventTrackItem(3, 4, source);

        processedEventTrackingRepository.save(processedEventTrackItem_1);
        processedEventTrackingRepository.save(processedEventTrackItem_2);
        processedEventTrackingRepository.save(processedEventTrackItem_3);
        processedEventTrackingRepository.save(processedEventTrackItem_4);

        final Stream<ProcessedEventTrackItem> allProcessedEvents = processedEventTrackingRepository.getAllProcessedEvents(source);

        final List<ProcessedEventTrackItem> processedEventTrackItems = allProcessedEvents.collect(toList());

        assertThat(processedEventTrackItems.size(), is(4));

        assertThat(processedEventTrackItems.get(0), is(processedEventTrackItem_1));
        assertThat(processedEventTrackItems.get(1), is(processedEventTrackItem_2));
        assertThat(processedEventTrackItems.get(2), is(processedEventTrackItem_3));
        assertThat(processedEventTrackItems.get(3), is(processedEventTrackItem_4));
    }

    @Test
    public void shouldReturnProcessedEventsInCorrectOrderEventIfInsertedOutOfOrder() throws Exception {

        final String source = "example-context";

        final ProcessedEventTrackItem processedEventTrackItem_1 = new ProcessedEventTrackItem(0, 1, source);
        final ProcessedEventTrackItem processedEventTrackItem_2 = new ProcessedEventTrackItem(1, 2, source);
        final ProcessedEventTrackItem processedEventTrackItem_3 = new ProcessedEventTrackItem(2, 3, source);
        final ProcessedEventTrackItem processedEventTrackItem_4 = new ProcessedEventTrackItem(3, 4, source);

        processedEventTrackingRepository.save(processedEventTrackItem_2);
        processedEventTrackingRepository.save(processedEventTrackItem_4);
        processedEventTrackingRepository.save(processedEventTrackItem_1);
        processedEventTrackingRepository.save(processedEventTrackItem_3);

        final Stream<ProcessedEventTrackItem> allProcessedEvents = processedEventTrackingRepository.getAllProcessedEvents(source);

        final List<ProcessedEventTrackItem> processedEventTrackItems = allProcessedEvents.collect(toList());

        assertThat(processedEventTrackItems.size(), is(4));

        assertThat(processedEventTrackItems.get(0), is(processedEventTrackItem_1));
        assertThat(processedEventTrackItems.get(1), is(processedEventTrackItem_2));
        assertThat(processedEventTrackItems.get(2), is(processedEventTrackItem_3));
        assertThat(processedEventTrackItems.get(3), is(processedEventTrackItem_4));
    }

    @Test
    public void shouldReturnOnlyReturnProcessedEventsWIthTheCorrectSOurce() throws Exception {

        final String source = "example-context";
        final String otherSource = "another-context";

        final ProcessedEventTrackItem processedEventTrackItem_1 = new ProcessedEventTrackItem(0, 1, source);
        final ProcessedEventTrackItem processedEventTrackItem_2 = new ProcessedEventTrackItem(1, 2, source);
        final ProcessedEventTrackItem processedEventTrackItem_3 = new ProcessedEventTrackItem(2, 3, source);
        final ProcessedEventTrackItem processedEventTrackItem_4 = new ProcessedEventTrackItem(3, 4, source);

        final ProcessedEventTrackItem processedEventTrackItem_5 = new ProcessedEventTrackItem(0, 1, otherSource);
        final ProcessedEventTrackItem processedEventTrackItem_6 = new ProcessedEventTrackItem(1, 2, otherSource);
        final ProcessedEventTrackItem processedEventTrackItem_7 = new ProcessedEventTrackItem(2, 3, otherSource);
        final ProcessedEventTrackItem processedEventTrackItem_8 = new ProcessedEventTrackItem(3, 4, otherSource);
        final ProcessedEventTrackItem processedEventTrackItem_9 = new ProcessedEventTrackItem(5, 6, otherSource);

        processedEventTrackingRepository.save(processedEventTrackItem_2);
        processedEventTrackingRepository.save(processedEventTrackItem_4);
        processedEventTrackingRepository.save(processedEventTrackItem_1);
        processedEventTrackingRepository.save(processedEventTrackItem_6);
        processedEventTrackingRepository.save(processedEventTrackItem_7);
        processedEventTrackingRepository.save(processedEventTrackItem_5);
        processedEventTrackingRepository.save(processedEventTrackItem_8);
        processedEventTrackingRepository.save(processedEventTrackItem_3);
        processedEventTrackingRepository.save(processedEventTrackItem_9);

        final Stream<ProcessedEventTrackItem> allProcessedEvents = processedEventTrackingRepository.getAllProcessedEvents(source);

        final List<ProcessedEventTrackItem> processedEventTrackItems = allProcessedEvents.collect(toList());

        assertThat(processedEventTrackItems.size(), is(4));

        assertThat(processedEventTrackItems.get(0), is(processedEventTrackItem_1));
        assertThat(processedEventTrackItems.get(1), is(processedEventTrackItem_2));
        assertThat(processedEventTrackItems.get(2), is(processedEventTrackItem_3));
        assertThat(processedEventTrackItems.get(3), is(processedEventTrackItem_4));
    }

    @Test
    public void shouldGetTheLatestProcessedEvent() throws Exception {

        final String source = "example-context";

        final ProcessedEventTrackItem processedEventTrackItem_1 = new ProcessedEventTrackItem(0, 1, source);
        final ProcessedEventTrackItem processedEventTrackItem_2 = new ProcessedEventTrackItem(1, 2, source);
        final ProcessedEventTrackItem processedEventTrackItem_3 = new ProcessedEventTrackItem(2, 3, source);
        final ProcessedEventTrackItem processedEventTrackItem_4 = new ProcessedEventTrackItem(3, 4, source);
        final ProcessedEventTrackItem processedEventTrackItem_5 = new ProcessedEventTrackItem(99, 100, "a-different-context");

        processedEventTrackingRepository.save(processedEventTrackItem_2);
        processedEventTrackingRepository.save(processedEventTrackItem_5);
        processedEventTrackingRepository.save(processedEventTrackItem_4);
        processedEventTrackingRepository.save(processedEventTrackItem_1);
        processedEventTrackingRepository.save(processedEventTrackItem_3);

        final Optional<ProcessedEventTrackItem> latestProcessedEvent = processedEventTrackingRepository.getLatestProcessedEvent(source);

        if (latestProcessedEvent.isPresent()) {

            final ProcessedEventTrackItem processedEventTrackItem = latestProcessedEvent.get();

            assertThat(processedEventTrackItem.getEventNumber(), is(4L));
            assertThat(processedEventTrackItem.getPreviousEventNumber(), is(3L));
            assertThat(processedEventTrackItem.getSource(), is(source));

        } else {
            fail();
        }
    }
}
