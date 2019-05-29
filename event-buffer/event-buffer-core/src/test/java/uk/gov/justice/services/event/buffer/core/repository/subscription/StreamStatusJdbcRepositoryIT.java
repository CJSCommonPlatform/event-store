package uk.gov.justice.services.event.buffer.core.repository.subscription;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class StreamStatusJdbcRepositoryIT {

    public static final String EVENT_LISTENER = "EVENT_LISTENER";

    private static final String COUNT_BY_STREAM_ID = "SELECT count(*) FROM stream_status WHERE stream_id=?";

    private static final long INITIAL_POSITION = 0L;

    private StreamStatusJdbcRepository streamStatusJdbcRepository;

    private DataSource dataSource;

    private PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();

    @Before
    public void initDatabase() throws Exception {
        dataSource = new FrameworkTestDataSourceFactory().createViewStoreDataSource();

        streamStatusJdbcRepository = new StreamStatusJdbcRepository(dataSource, preparedStatementWrapperFactory);

        new DatabaseCleaner().cleanViewStoreTables("framework", "stream_status", "stream_buffer");
    }

    @Test
    public void shouldNotCreateSeparateInitialSubscriptinForTheNewSourceWhenWeHaveExistingEventsForTheStream() throws Exception {
        final String source = "unknown";
        final UUID streamId = randomUUID();

        initialiseBuffer(streamId, source);
        final Subscription subscription = new Subscription(streamId, 2L, source, EVENT_LISTENER);

        streamStatusJdbcRepository.update(subscription);

        initialiseBuffer(streamId, "sjp");
        final int count = countByStreamId(streamId);
        assertThat(count, is(1));

        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, "sjp", EVENT_LISTENER);

        assertThat(result.get().getSource(), is("sjp"));
        assertThat(result.get().getPosition(), is(2L));
    }

    @Test
    public void shouldCreateSeparateInitialSubscriptionForTheNewSourceWhenWeHaveNoExistingEventsForTheStream() throws Exception {
        final String source = "sjp";
        final UUID streamId = randomUUID();

        initialiseBuffer(streamId, source);
        final int count = countByStreamId(streamId);
        assertThat(count, is(1));

        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source, EVENT_LISTENER);

        assertThat(result.get().getSource(), is(source));
        assertThat(result.get().getPosition(), is(0L));
    }

    @Test
    public void shouldAppendToExistingSubscriptionForTheNewSourceWhenWeHaveExistingEventsForTheStream() throws Exception {
        final String source = "sjp";
        final UUID streamId = randomUUID();

        initialiseBuffer(streamId, source);
        final Subscription subscription = new Subscription(streamId, 2L, source, EVENT_LISTENER);

        streamStatusJdbcRepository.update(subscription);

        initialiseBuffer(streamId, source);
        final int count = countByStreamId(streamId);
        assertThat(count, is(1));

        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source, EVENT_LISTENER);

        assertThat(result.get().getSource(), is("sjp"));
        assertThat(result.get().getPosition(), is(2L));
    }


    @Test
    public void shouldUpdateSourceWhenUnknown() throws Exception {
        final String source = "unknown";
        final UUID streamId = randomUUID();

        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 1L, source, EVENT_LISTENER));

        final Subscription subscription = new Subscription(streamId, 2L, source, EVENT_LISTENER);

        streamStatusJdbcRepository.update(subscription);
        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source, EVENT_LISTENER);
        assertThat(result.get().getSource(), is(source));
        assertThat(result.get().getPosition(), is(2L));
    }

    @Test
    public void shouldUpdateSourceWhenNotUnknown() throws Exception {
        final UUID streamId = randomUUID();

        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 1L, "unknown", EVENT_LISTENER));

        final String source = "sjp";
        final Subscription subscription = new Subscription(streamId, 2L, source, EVENT_LISTENER);

        streamStatusJdbcRepository.update(subscription);
        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source, EVENT_LISTENER);
        assertThat(result.get().getSource(), is(source));
        assertThat(result.get().getPosition(), is(2L));
    }

    @Test
    public void shouldInsertAndReturnSubscription() throws Exception {
        final UUID id = randomUUID();
        final long version = 4L;
        final String source = "source";

        streamStatusJdbcRepository.insert(subscriptionOf(id, version, source, EVENT_LISTENER));

        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(id, source, EVENT_LISTENER);
        assertTrue(result.isPresent());
        assertThat(result.get().getPosition(), is(version));
        assertThat(result.get().getSource(), is(source));

    }

    @Test
    public void shouldReturnOptionalNotPresentIfStatusNotFound() throws Exception {
        Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(randomUUID(), "source", EVENT_LISTENER);
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldUpdateVersionForSameStreamIdWithMultipleSources() throws Exception {
        final UUID id = randomUUID();
        streamStatusJdbcRepository.insert(subscriptionOf(id, 4L, "source 4", EVENT_LISTENER));
        streamStatusJdbcRepository.update(subscriptionOf(id, 5L, "source 4", EVENT_LISTENER));
        streamStatusJdbcRepository.insert(subscriptionOf(id, 4L, "source 5", EVENT_LISTENER));
        streamStatusJdbcRepository.update(subscriptionOf(id, 5L, "source 5", EVENT_LISTENER));

        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(id, "source 5", EVENT_LISTENER);
        assertTrue(result.isPresent());
        assertThat(result.get().getPosition(), is(5L));
        assertThat(result.get().getSource(), is("source 5"));

    }

    @Test
    public void shouldNotUpdateVersionForANewSourceField() throws Exception {
        final UUID streamId = randomUUID();
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 1L, "source2", EVENT_LISTENER));

        final String source3 = "source3";
        final Subscription subscription = new Subscription(streamId, 1L, source3, EVENT_LISTENER);

        streamStatusJdbcRepository.update(subscription);
        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source3, EVENT_LISTENER);
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void shouldUpdateVersionForAnExistingSourceField() throws Exception {
        final UUID streamId = randomUUID();
        final String source3 = "source3";
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 4L, source3, EVENT_LISTENER));
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 1L, "source4", EVENT_LISTENER));

        final Subscription subscription = new Subscription(streamId, 5L, source3, EVENT_LISTENER);

        streamStatusJdbcRepository.update(subscription);
        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source3, EVENT_LISTENER);
        assertTrue(result.isPresent());
        assertThat(result.get().getPosition(), is(5L));
        assertThat(result.get().getSource(), is(source3));
    }


    @Test
    public void shouldUpdateNewVersionNumberForExistingSourceWhenMultipleSourceEventsExist() throws Exception {
        final UUID streamId = randomUUID();
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 1L, "source1", EVENT_LISTENER));
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 1L, "source2", EVENT_LISTENER));
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 2L, "source3", EVENT_LISTENER));

        final String existingSource = "source2";
        final Subscription subscription = new Subscription(streamId, 2L, existingSource, EVENT_LISTENER);

        streamStatusJdbcRepository.update(subscription);
        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, existingSource, EVENT_LISTENER);
        assertTrue(result.isPresent());
        assertThat(result.get().getPosition(), is(2L));
        assertThat(result.get().getSource(), is(existingSource));
    }

    @Test
    public void shouldNotUpdateNewVersionNumberForNewSourceWhenMultipleSourceEventsExist() throws Exception {
        final UUID streamId = randomUUID();
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 1L, "source1", EVENT_LISTENER));
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 1L, "source2", EVENT_LISTENER));
        streamStatusJdbcRepository.insert(subscriptionOf(streamId, 2L, "source3", EVENT_LISTENER));

        final String newSource = "source4";
        final Subscription subscription = new Subscription(streamId, 1L, newSource, EVENT_LISTENER);

        streamStatusJdbcRepository.update(subscription);
        final Optional<Subscription> result = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, newSource, EVENT_LISTENER);
        assertThat(result, is(Optional.empty()));
    }

    private Subscription subscriptionOf(final UUID id, final Long version, final String source, final String component) {
        return new Subscription(id, version, source, component);
    }

    private long initialiseBuffer(final UUID streamId, final String source) {
        streamStatusJdbcRepository.updateSource(streamId, source, EVENT_LISTENER);
        final Optional<Subscription> currentStatus = streamStatusJdbcRepository.findByStreamIdAndSource(streamId, source, EVENT_LISTENER);

        if (!currentStatus.isPresent()) {
            //this is to address race condition
            //in case of primary key violation the exception gets thrown, event goes back into topic and the transaction gets retried
            streamStatusJdbcRepository
                    .insert(new Subscription(streamId, INITIAL_POSITION, source, EVENT_LISTENER));
            return INITIAL_POSITION;

        } else {
            return currentStatus.get().getPosition();
        }
    }

    /**
     * Returns a count of records for a given stream streamId.
     *
     * @param streamId streamId of the stream.
     * @return a int.
     */
    public int countByStreamId(final UUID streamId) {
        try (final PreparedStatementWrapper ps = preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, COUNT_BY_STREAM_ID)) {
            ps.setObject(1, streamId);
            final ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new JdbcRepositoryException(format("Exception while looking up status of the stream: %s", streamId), e);
        }
        return 0;
    }

}
