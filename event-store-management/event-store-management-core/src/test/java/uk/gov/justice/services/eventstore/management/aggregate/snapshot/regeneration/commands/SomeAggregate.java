package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

import uk.gov.justice.domain.aggregate.Aggregate;

import java.util.stream.Stream;

public class SomeAggregate implements Aggregate {

    @Override
    public Object apply(final Object event) {
        throw new UnsupportedOperationException("Dont' care.");
    }

    @Override
    public Stream<Object> apply(final Stream<Object> events) {
        throw new UnsupportedOperationException("Dont' care.");
    }

    @Override
    public void applyForEach(final Stream<Object> events) {
        throw new UnsupportedOperationException("Dont' care.");
    }
}
