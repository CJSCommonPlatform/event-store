package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AggregateSnapshotGenerationCommandConfigurationTest {

    @InjectMocks
    private AggregateSnapshotGenerationCommandConfiguration aggregateSnapshotGenerationCommandConfiguration;

    @Test
    public void shouldGetTheJndiTimeoutValue() throws Exception {

        final String timeout = "23";

        setField(aggregateSnapshotGenerationCommandConfiguration, "transactionTimoutSeconds", timeout);

        assertThat(aggregateSnapshotGenerationCommandConfiguration.getTransactionTimoutSeconds(), is(23));
    }

    @Test
    public void shouldGetDefaultValueIfNoJndiValueSet() throws Exception {

        assertThat(aggregateSnapshotGenerationCommandConfiguration.getTransactionTimoutSeconds(), is(20 * 60 * 60));
    }

    @Test
    public void shouldThrowSensibleExceptionIfJndiValueCannotBeParsedToInt() throws Exception {

        final String timeout = "something-that-is-not-a-number";

        setField(aggregateSnapshotGenerationCommandConfiguration, "transactionTimoutSeconds", timeout);

        final AggregateSnapshotGenerationFailedException aggregateSnapshotGenerationFailedException = assertThrows(
                AggregateSnapshotGenerationFailedException.class,
                () -> aggregateSnapshotGenerationCommandConfiguration.getTransactionTimoutSeconds());

        assertThat(aggregateSnapshotGenerationFailedException.getMessage(), is("Failed to parse jndi value 'jmx.aggregate.snapshot.generation.timout.seconds'. Value 'something-that-is-not-a-number' is not a number"));

    }
}