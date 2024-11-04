package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

import static java.lang.String.format;

import uk.gov.justice.domain.aggregate.Aggregate;

public class AggregateClassProvider {

    @SuppressWarnings("unchecked")
    public Class<? extends Aggregate> toClass(final String aggregateClassName) {

        try {
            final Class<?> aClass = Class.forName(aggregateClassName);

            if (Aggregate.class.isAssignableFrom(aClass)) {
                return (Class<? extends Aggregate>) aClass;
            }

            throw new AggregateClassLoadingException(format("The class '%s' is not an Aggregate", aggregateClassName));

        } catch (final ClassNotFoundException e) {
            throw new AggregateClassLoadingException(format("Failed to load Aggregate class '%s'",  aggregateClassName), e);
        }
    }
}
