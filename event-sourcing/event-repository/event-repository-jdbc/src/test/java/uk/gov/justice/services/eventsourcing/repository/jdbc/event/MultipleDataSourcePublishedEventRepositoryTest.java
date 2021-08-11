package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapper;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import java.sql.SQLException;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MultipleDataSourcePublishedEventRepositoryTest {
    private static final String SQL_FIND_ALL_SINCE = "SELECT * FROM published_event WHERE event_number > ? ORDER BY event_number ASC";
    private static final String SQL_FIND_RANGE = "SELECT * FROM published_event WHERE event_number >= ? AND event_number < ? ORDER BY event_number ASC";

    @Mock
    private JdbcResultSetStreamer jdbcResultSetStreamer;
    @Mock
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;
    @Mock
    private DataSource dataSource;
    @Mock
    private PreparedStatementWrapper preparedStatementWrapper;
    @Mock
    private Stream<PublishedEvent> publishedEventStream;

    @InjectMocks
    private MultipleDataSourcePublishedEventRepository repository;

    @Before
    public void before() throws SQLException {
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_ALL_SINCE)).thenReturn(preparedStatementWrapper);
        when(preparedStatementWrapperFactory.preparedStatementWrapperOf(dataSource, SQL_FIND_RANGE)).thenReturn(preparedStatementWrapper);

        when(jdbcResultSetStreamer.streamOf(any(PreparedStatementWrapper.class), any(Function.class))).thenReturn(publishedEventStream);
    }

    @Test
    public void shouldSetFetchSizeWhenCallingFindEventsSince() throws SQLException {
        try (final Stream<PublishedEvent> stream = repository.findEventsSince(0);) {
            assertThat(stream, Matchers.is(publishedEventStream));
        }

        verify(preparedStatementWrapper).setFetchSize();
    }

    @Test
    public void shouldSetFetchSizeWhenCallingFindEventRange() throws SQLException {

        try (final Stream<PublishedEvent> stream = repository.findEventRange(0, 1)) {
            assertThat(stream, Matchers.is(publishedEventStream));
        }

        verify(preparedStatementWrapper).setFetchSize();
    }
}