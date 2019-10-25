package uk.gov.justice.services.subscription;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.eventsourcing.source.api.streams.MissingEventRange;
import uk.gov.justice.services.eventsourcing.util.messaging.EventSourceNameCalculator;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class ProcessedEventTrackingServiceIT {

    private final DataSource viewStoreDataSource = new FrameworkTestDataSourceFactory().createViewStoreDataSource();
    private final JdbcResultSetStreamer jdbcResultSetStreamer = new JdbcResultSetStreamer();
    private final PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();
    private final ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider = new TestViewStoreJdbcDataSourceProvider(viewStoreDataSource);
    private final ProcessedEventTrackingRepository processedEventTrackingRepository = new ProcessedEventTrackingRepository();
    private final EventSourceNameCalculator eventSourceNameCalculator = new EventSourceNameCalculator();
    private final Logger logger = getLogger(ProcessedEventTrackingService.class);

    private final ProcessedEventTrackingService processedEventTrackingService = new ProcessedEventTrackingService();

    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();

    @Before
    public void createClassUnderTest() {
        setField(processedEventTrackingRepository, "jdbcResultSetStreamer", jdbcResultSetStreamer);
        setField(processedEventTrackingRepository, "preparedStatementWrapperFactory", preparedStatementWrapperFactory);
        setField(processedEventTrackingRepository, "viewStoreJdbcDataSourceProvider", viewStoreJdbcDataSourceProvider);

        setField(processedEventTrackingService, "processedEventTrackingRepository", processedEventTrackingRepository);
        setField(processedEventTrackingService, "eventSourceNameCalculator", eventSourceNameCalculator);
        setField(processedEventTrackingService, "logger", logger);

        databaseCleaner.cleanProcessedEventTable("framework");
    }

    @Test
    public void shouldFindRangesOfMissingEventsInAscendingOrderOfEventNumbers() throws Exception {

        final String source = "example-context";
        final String componentName = "EVENT_LISTENER";

        // insert events missing event 4 and events 7, 8 and 9
        insertEventsWithSomeMissing(source, componentName);

        final List<MissingEventRange> missingEventRanges = processedEventTrackingService
                .getAllMissingEvents(source, componentName)
                .collect(toList());

        assertThat(missingEventRanges.size(), is(3));

        assertThat(missingEventRanges.get(0), is(new MissingEventRange(4L, 5L)));
        assertThat(missingEventRanges.get(1), is(new MissingEventRange(7L, 10L)));
        assertThat(missingEventRanges.get(2), is(new MissingEventRange(11L, 9223372036854775807L)));
    }

    private void insertEventsWithSomeMissing(final String source, final String componentName) {

        final ProcessedEventTrackItem processedEventTrackItem_1 = new ProcessedEventTrackItem(0, 1, source, componentName);
        final ProcessedEventTrackItem processedEventTrackItem_2 = new ProcessedEventTrackItem(1, 2, source, componentName);
        final ProcessedEventTrackItem processedEventTrackItem_3 = new ProcessedEventTrackItem(2, 3, source, componentName);

        final ProcessedEventTrackItem processedEventTrackItem_5 = new ProcessedEventTrackItem(4, 5, source, componentName);
        final ProcessedEventTrackItem processedEventTrackItem_6 = new ProcessedEventTrackItem(5, 6, source, componentName);

        final ProcessedEventTrackItem processedEventTrackItem_10 = new ProcessedEventTrackItem(9, 10, source, componentName);

        processedEventTrackingRepository.save(processedEventTrackItem_1);
        processedEventTrackingRepository.save(processedEventTrackItem_2);
        processedEventTrackingRepository.save(processedEventTrackItem_3);

        processedEventTrackingRepository.save(processedEventTrackItem_5);
        processedEventTrackingRepository.save(processedEventTrackItem_6);

        processedEventTrackingRepository.save(processedEventTrackItem_10);
    }
}
