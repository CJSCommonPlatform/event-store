package uk.gov.justice.services.eventstore.management.verification.process;

public enum LinkedEventNumberTable {

    PUBLISHED_EVENT("published_event"),
    PROCESSED_EVENT("processed_event");

    private final String tableName;

    LinkedEventNumberTable(final String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
