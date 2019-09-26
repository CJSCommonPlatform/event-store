package uk.gov.justice.services.eventstore.management.events.catchup;

public enum CatchupType {

    EVENT_CATCHUP("Event"),
    INDEX_CATCHUP("Index");

    private final String name;

    CatchupType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
