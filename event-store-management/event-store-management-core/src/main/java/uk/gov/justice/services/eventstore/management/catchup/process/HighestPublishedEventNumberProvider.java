package uk.gov.justice.services.eventstore.management.catchup.process;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class HighestPublishedEventNumberProvider {

    @Inject
    private PublishedEventRepository publishedEventRepository;

    @Transactional
    public Long getHighestPublishedEventNumber() {

        return publishedEventRepository.getLatestPublishedEvent()
                .map(publishedEvent -> publishedEvent
                        .getEventNumber()
                        .orElse(0L))
                .orElse(0L);
    }

}
