package uk.gov.justice.services.eventsourcing.publishedevent.jdbc;

public interface PublishedEventStatements {

    String TRUNCATE_PUBLISHED_EVENT = "TRUNCATE published_event";
    String TRUNCATE_PREPUBLISH_QUEUE = "TRUNCATE pre_publish_queue";

    String INSERT_INTO_PUBLISHED_EVENT_SQL = "INSERT into published_event (" +
            "id, stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number) " +
            "VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String SELECT_FROM_PUBLISHED_EVENT_QUERY =
            "SELECT stream_id, position_in_stream, name, payload, metadata, date_created, event_number, previous_event_number " +
                    "FROM published_event " +
                    "WHERE id = ?";
}
