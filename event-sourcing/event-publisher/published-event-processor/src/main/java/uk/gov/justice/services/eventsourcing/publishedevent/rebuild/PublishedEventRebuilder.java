package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static javax.transaction.Transactional.TxType.NOT_SUPPORTED;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventTableCleaner;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class PublishedEventRebuilder {

    @Inject
    private EventNumberRenumberer eventNumberRenumberer;

    @Inject
    private PublishedEventTableCleaner publishedEventTableCleaner;

    @Inject
    private PublishedEventUpdater publishedEventUpdater;

    @Transactional(NOT_SUPPORTED)
    public void rebuild() {

        eventNumberRenumberer.renumberEventLogEventNumber();
        publishedEventTableCleaner.deleteAll();
        publishedEventUpdater.createPublishedEvents();
    }
}
