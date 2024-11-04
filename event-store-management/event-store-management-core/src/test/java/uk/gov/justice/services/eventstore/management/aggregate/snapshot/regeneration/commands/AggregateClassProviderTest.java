package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import uk.gov.justice.domain.aggregate.Aggregate;

@ExtendWith(MockitoExtension.class)
public class AggregateClassProviderTest {

    @InjectMocks
    private AggregateClassProvider aggregateClassProvider;

    @Test
    public void shouldGetTheClassOfAnAggregateByItsName() throws Exception {

        final Class<? extends Aggregate> anAggregateClass = aggregateClassProvider.toClass(SomeAggregate.class.getName());

        assertThat(anAggregateClass.getName(), is(SomeAggregate.class.getName()));
    }

    @Test
    public void shouldFailIfClassIsNotAnAggregate() throws Exception {

        final AggregateClassLoadingException aggregateClassLoadingException = assertThrows(
                AggregateClassLoadingException.class,
                () -> aggregateClassProvider.toClass(SomeClassThatIsNotAnAggregate.class.getName()));

        assertThat(aggregateClassLoadingException.getMessage(), is("The class 'uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands.SomeClassThatIsNotAnAggregate' is not an Aggregate"));
    }

    @Test
    public void shouldFailIfClassNotFound() throws Exception {

        final String someMissingClassName = "uk.gov.justice.services.eventstore.ThisClassIsNotOnTheClasspath";

        final AggregateClassLoadingException aggregateClassLoadingException = assertThrows(
                AggregateClassLoadingException.class,
                () -> aggregateClassProvider.toClass(someMissingClassName));

        assertThat(aggregateClassLoadingException.getMessage(), is("Failed to load Aggregate class 'uk.gov.justice.services.eventstore.ThisClassIsNotOnTheClasspath'"));
        assertThat(aggregateClassLoadingException.getCause(), is(instanceOf(ClassNotFoundException.class)));
    }
}