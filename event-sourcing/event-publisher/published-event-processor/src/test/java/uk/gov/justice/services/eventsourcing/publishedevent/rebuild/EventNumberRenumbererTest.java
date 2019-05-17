package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.datasource.DefaultEventStoreDataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventNumberRenumbererTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private DefaultEventStoreDataSourceProvider defaultEventStoreDataSourceProvider;

    @InjectMocks
    private EventNumberRenumberer eventNumberRenumberer;

    @Test
    public void shouldResetTheEventNumberSequenceAndRenumberAllEvents() throws Exception {

        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement_1 = mock(PreparedStatement.class);
        final PreparedStatement preparedStatement_2 = mock(PreparedStatement.class);

        when(defaultEventStoreDataSourceProvider.getDefaultDataSource().getConnection()).thenReturn(connection);
        when(connection.prepareStatement("ALTER SEQUENCE event_sequence_seq RESTART WITH 1")).thenReturn(preparedStatement_1);
        when(connection.prepareStatement("UPDATE event_log SET event_number = nextval('event_sequence_seq')")).thenReturn(preparedStatement_2);

        eventNumberRenumberer.renumberEventLogEventNumber();

        final InOrder inOrder = inOrder(preparedStatement_1, preparedStatement_2);

        inOrder.verify(preparedStatement_1).executeUpdate();
        inOrder.verify(preparedStatement_2).executeUpdate();
    }
}
