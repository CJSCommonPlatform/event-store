package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventStatements.INSERT_INTO_PUBLISHED_EVENT_SQL;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventsourcing.util.io.Closer;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BatchedPublishedEventInserterFactoryTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private Closer closer;

    @InjectMocks
    private BatchedPublishedEventInserterFactory batchedPublishedEventInserterFactory;

    @Test
    public void shouldCreateCorrectlyInitialisedEventInserter() throws Exception {

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(eventStoreDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(INSERT_INTO_PUBLISHED_EVENT_SQL)).thenReturn(preparedStatement);

        final BatchedPublishedEventInserter batchedPublishedEventInserter = batchedPublishedEventInserterFactory.createInitialised();

        assertThat(getValueOfField(batchedPublishedEventInserter, "closer", Closer.class), is(closer));
        assertThat(getValueOfField(batchedPublishedEventInserter, "connection", Connection.class), is(connection));
        assertThat(getValueOfField(batchedPublishedEventInserter, "preparedStatement", PreparedStatement.class), is(preparedStatement));
    }
}
