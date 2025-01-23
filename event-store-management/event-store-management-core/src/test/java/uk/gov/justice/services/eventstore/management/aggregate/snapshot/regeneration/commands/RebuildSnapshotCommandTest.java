package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RebuildSnapshotCommandTest {

    @InjectMocks
    private RebuildSnapshotCommand rebuildSnapshotCommand;

    @Test
    public void shouldRequireACommandRuntimeId() throws Exception {
        assertThat(rebuildSnapshotCommand.requiresCommandRuntimeId(), is(true));
        assertThat(rebuildSnapshotCommand.commandRuntimeIdType(), is("streamId"));
    }

    @Test
    public void shouldRequireACommandRuntimeString() throws Exception {
        assertThat(rebuildSnapshotCommand.requiresCommandRuntimeString(), is(true));
        assertThat(rebuildSnapshotCommand.commandRuntimeStringType(), is("aggregate class name"));
    }

    @Test
    public void shouldHaveTheCorrectDescription() throws Exception {
        assertThat(rebuildSnapshotCommand.getDescription(), is("Forces the generation of a new aggregate snapshot for a given streamId and aggregate class name"));
    }
}