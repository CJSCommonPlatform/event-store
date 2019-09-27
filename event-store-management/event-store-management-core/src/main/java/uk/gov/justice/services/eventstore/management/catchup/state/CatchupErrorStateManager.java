package uk.gov.justice.services.eventstore.management.catchup.state;

import static com.google.common.collect.ImmutableList.copyOf;
import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.EVENT_CATCHUP;

import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CatchupErrorStateManager {

    private final List<CatchupError> eventCatchupErrors = new CopyOnWriteArrayList<>();
    private final List<CatchupError> indexCatchupErrors = new CopyOnWriteArrayList<>();

    public void add(final CatchupError catchupError, final CatchupType catchupType) {
        listFor(catchupType).add(catchupError);
    }

    public List<CatchupError> getErrors(final CatchupType catchupType) {
        return copyOf(listFor(catchupType));
    }

    public void clear(final CatchupType catchupType) {
        listFor(catchupType).clear();
    }

    private List<CatchupError> listFor(final CatchupType catchupType) {
        if (catchupType == EVENT_CATCHUP) {
            return eventCatchupErrors;
        }

        return indexCatchupErrors;
    }
}
