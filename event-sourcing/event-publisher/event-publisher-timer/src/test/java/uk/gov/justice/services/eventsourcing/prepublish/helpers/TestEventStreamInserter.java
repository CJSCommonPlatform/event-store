package uk.gov.justice.services.eventsourcing.prepublish.helpers;

import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.sql.DataSource;

public class TestEventStreamInserter {

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

    private static final String INSERT_INTO_EVENT_STREAM =
            "INSERT INTO event_stream (" +
                    "stream_id, position_in_stream, active, date_created" +
                    ") VALUES (?, ?, ?, ?)";

    public void insertIntoEventStream(
            final UUID streamId,
            final long position_in_stream,
            final boolean active,
            final ZonedDateTime dateCreated
    ) throws SQLException {
        try (final Connection connection = eventStoreDataSource.getConnection()) {

            try (final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_EVENT_STREAM)) {
                preparedStatement.setObject(1, streamId);
                preparedStatement.setLong(2, position_in_stream);
                preparedStatement.setBoolean(3, active);
                preparedStatement.setObject(4, toSqlTimestamp(dateCreated));

                preparedStatement.executeUpdate();
            }
        }
    }
}
