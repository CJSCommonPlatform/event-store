package uk.gov.justice.services.eventstore.management.catchup.commands;

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
