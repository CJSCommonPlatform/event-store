package uk.gov.justice.services.eventsourcing.publishedevent.rebuild.integration.helpers;

import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber.BatchEventRenumberer;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber.EventIdsByBatchProvider;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber.EventNumberRenumberer;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.renumber.EventNumberSequenceResetter;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;

import org.slf4j.Logger;

public class EventNumberRenumbererFactory {

    public EventNumberRenumberer eventNumberRenumberer(
            final EventStoreDataSourceProvider eventStoreDataSourceProvider,
            final Logger logger) {


        final EventNumberRenumberer eventNumberRenumberer = new EventNumberRenumberer();
        final BatchEventRenumberer batchEventRenumberer = batchEventRenumberer(eventStoreDataSourceProvider);

        final EventNumberSequenceResetter eventNumberSequenceResetter = eventNumberSequenceResetter(
                eventStoreDataSourceProvider
        );

        setField(eventNumberRenumberer, "batchEventRenumberer", batchEventRenumberer);
        setField(eventNumberRenumberer, "eventNumberSequenceResetter", eventNumberSequenceResetter);
        setField(eventNumberRenumberer, "logger", logger);

        return eventNumberRenumberer;
    }

    private BatchEventRenumberer batchEventRenumberer(
            final EventStoreDataSourceProvider eventStoreDataSourceProvider) {

        final BatchEventRenumberer batchEventRenumberer = new BatchEventRenumberer();

        setField(batchEventRenumberer, "eventStoreDataSourceProvider", eventStoreDataSourceProvider);
        setField(batchEventRenumberer, "eventIdsByBatchProvider", new EventIdsByBatchProvider());

        return batchEventRenumberer;
    }

    private EventNumberSequenceResetter eventNumberSequenceResetter(final EventStoreDataSourceProvider eventStoreDataSourceProvider) {

        final EventNumberSequenceResetter eventNumberSequenceResetter = new EventNumberSequenceResetter();

        setField(eventNumberSequenceResetter, "eventStoreDataSourceProvider", eventStoreDataSourceProvider);

        return eventNumberSequenceResetter;
    }
}
