package uk.gov.justice.services.eventsourcing.publishing;

import static java.lang.Thread.currentThread;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishing.helpers.EventStoreInitializer;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SequenceNumberUpdaterIT {

    private final DataSource dataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

    @Mock
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @InjectMocks
    private SequenceNumberUpdater sequenceNumberUpdater;

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(dataSource);
    }

    @Test
    public void shouldInsertTheNextSequenceNumberAndUpdateThePreviousAndNextColumns() throws Exception {

        when(subscriptionDataSourceProvider.getEventStoreDataSource()).thenReturn(dataSource);

        final UUID eventId_1 = randomUUID();
        final UUID eventId_2 = randomUUID();
        final UUID eventId_3 = randomUUID();
        final UUID eventId_4 = randomUUID();

        assertThat(sequenceNumberUpdater.update(eventId_1), is(1L));
        assertThat(sequenceNumberUpdater.update(eventId_2), is(2L));
        assertThat(sequenceNumberUpdater.update(eventId_3), is(3L));
        assertThat(sequenceNumberUpdater.update(eventId_4), is(4L));

        checkRow(eventId_1, 1, 0, 2);
        checkRow(eventId_2, 2, 1, 3);
        checkRow(eventId_3, 3, 2, 4);
        checkRow(eventId_4, 4, 3, 0);
    }

    @Test
    public void shouldHandleMultipleThreads() throws Exception {

        when(subscriptionDataSourceProvider.getEventStoreDataSource()).thenReturn(dataSource);

        final int numberOfThreads = 10;
        final int numberOfInsertsPerThread = 10;

        final List<Thread> threads = new ArrayList<>();
        final List<UUID> eventIds = new ArrayList<>();

        final StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        for (int i = 0; i < numberOfThreads; i++) {

            final Thread thread = new Thread(() -> {
                for (int j = 0; j < numberOfInsertsPerThread; j++) {
                    try {
                        final UUID eventId = randomUUID();
                        sequenceNumberUpdater.update(eventId);
                        eventIds.add(eventId);
                    } catch (final SQLException e) {
                        throw new RuntimeException("Failed to update sequence number", e);
                    }
                }
            });

            threads.add(thread);
            thread.start();
        }

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                currentThread().interrupt();
            }
        });

        stopWatch.stop();

        final int totalNumberOfEvents = numberOfThreads * numberOfInsertsPerThread;

        final long time = stopWatch.getTime();
        System.out.println("Sequencing " + totalNumberOfEvents + " events took " + time + " milliseconds. Average of " + (time / totalNumberOfEvents) + " milliseconds per event");

        checkRow(eventIds.get(0), 1, 0, 2);
        checkRow(eventIds.get(1), 2, 1, 3);
        checkRow(eventIds.get(2), 3, 2, 4);
        checkRow(eventIds.get(3), 4, 3, 5);
        checkRow(eventIds.get(4), 5, 4, 6);
        checkRow(eventIds.get(5), 6, 5, 7);
        checkRow(eventIds.get(6), 7, 6, 8);
        checkRow(eventIds.get(7), 8, 7, 9);
        checkRow(eventIds.get(8), 9, 8, 10);
        checkRow(eventIds.get(9), 10, 9, 11);
        checkRow(eventIds.get(10), 11, 10, 12);
        checkRow(eventIds.get(11), 12, 11, 13);
        checkRow(eventIds.get(12), 13, 12, 14);
        checkRow(eventIds.get(13), 14, 13, 15);
        checkRow(eventIds.get(14), 15, 14, 16);
        checkRow(eventIds.get(15), 16, 15, 17);
        checkRow(eventIds.get(16), 17, 16, 18);
        checkRow(eventIds.get(17), 18, 17, 19);
        checkRow(eventIds.get(18), 19, 18, 20);
//        checkRow(eventIds.get(19), 20, 19, 0);
    }

    private void checkRow(final UUID eventId, final long expectedSequenceNumber, final long expectedPrevious, final long expectedNext) throws Exception {

        try (final Connection connection = dataSource.getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT sequence_number, previous, next FROM event_sequence WHERE event_id = ? ORDER BY sequence_number")) {
                preparedStatement.setObject(1, eventId);

                try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                    assertThat(resultSet.next(), is(true));

                    assertThat(resultSet.getLong("sequence_number"), is(expectedSequenceNumber));
                    assertThat(resultSet.getLong("previous"), is(expectedPrevious));
                    assertThat(resultSet.getLong("next"), is(expectedNext));

                    assertThat(resultSet.next(), is(false));
                }
            }
        }
    }
}
