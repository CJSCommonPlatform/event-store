package uk.gov.justice.services.eventstore.management.verification.process;

import static java.lang.String.format;
import static uk.gov.justice.services.eventstore.management.verification.process.LinkedEventNumberTable.PUBLISHED_EVENT;

public class EventLinkageErrorMessageGenerator {

    public String generateErrorMessage(
            final int previousEventNumber,
            final int eventNumber,
            final int lastEventNumber,
            final LinkedEventNumberTable linkedEventNumberTable) {


        final String message;
        if (linkedEventNumberTable == PUBLISHED_EVENT) {
            message = "Events incorrectly linked in %s table: " +
                    "Event with event number %d " +
                    "is linked to previous event number %d " +
                    "whereas it should be %d";

        } else {
            message = "Events missing from %s table: " +
                    "Event with event_number %d " +
                    "has a previous_event_number of %d, " +
                    "but the event in the previous row in the database " +
                    "has an event_number of %d";
        }

        return format(
                message,
                linkedEventNumberTable.getTableName(),
                eventNumber,
                previousEventNumber,
                lastEventNumber);
    }
}
