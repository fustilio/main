package seedu.address.logic.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static seedu.address.logic.commands.CommandTestUtil.showFirstParcelOnly;
import static seedu.address.model.ModelManager.getDeliveredPredicate;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PARCEL;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PARCEL;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PARCEL;
import static seedu.address.testutil.TypicalParcels.getTypicalAddressBook;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.JumpToListRequestEvent;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.UndoRedoStack;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.ui.testutil.EventsCollectorRule;

/**
 * Contains integration tests (interaction with the Model) for {@code SelectCommand}.
 */
public class SelectCommandTest {
    @Rule
    public final EventsCollectorRule eventsCollectorRule = new EventsCollectorRule();

    private Model model;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Index lastParcelIndex = Index.fromOneBased(model.getActiveList().size());

        assertExecutionSuccess(INDEX_FIRST_PARCEL);
        assertExecutionSuccess(INDEX_THIRD_PARCEL);
        assertExecutionSuccess(lastParcelIndex);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getActiveList().size() + 1);

        assertExecutionFailure(outOfBoundsIndex, Messages.MESSAGE_INVALID_PARCEL_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showFirstParcelOnly(model);

        assertExecutionSuccess(INDEX_FIRST_PARCEL);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showFirstParcelOnly(model);

        Index outOfBoundsIndex = INDEX_SECOND_PARCEL;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundsIndex.getZeroBased() < model.getAddressBook().getParcelList().stream()
                .filter(getDeliveredPredicate().negate()).count());

        assertExecutionFailure(outOfBoundsIndex, Messages.MESSAGE_INVALID_PARCEL_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        SelectCommand selectFirstCommand = new SelectCommand(INDEX_FIRST_PARCEL);
        SelectCommand selectSecondCommand = new SelectCommand(INDEX_SECOND_PARCEL);

        // same object -> returns true
        assertTrue(selectFirstCommand.equals(selectFirstCommand));

        // same values -> returns true
        SelectCommand selectFirstCommandCopy = new SelectCommand(INDEX_FIRST_PARCEL);
        assertTrue(selectFirstCommand.equals(selectFirstCommandCopy));

        // different types -> returns false
        assertFalse(selectFirstCommand.equals(1));

        // null -> returns false
        assertFalse(selectFirstCommand.equals(null));

        // different parcel -> returns false
        assertFalse(selectFirstCommand.equals(selectSecondCommand));
    }

    /**
     * Executes a {@code SelectCommand} with the given {@code index}, and checks that {@code JumpToListRequestEvent}
     * is raised with the correct index.
     */
    private void assertExecutionSuccess(Index index) {
        SelectCommand selectCommand = prepareCommand(index);

        try {
            CommandResult commandResult = selectCommand.execute();
            assertEquals(String.format(SelectCommand.MESSAGE_SELECT_PARCEL_SUCCESS, index.getOneBased()),
                    commandResult.feedbackToUser);
        } catch (CommandException ce) {
            throw new IllegalArgumentException("Execution of command should not fail.", ce);
        }

        JumpToListRequestEvent lastEvent = (JumpToListRequestEvent) eventsCollectorRule.eventsCollector.getMostRecent();
        assertEquals(index, Index.fromZeroBased(lastEvent.targetIndex));
    }

    /**
     * Executes a {@code SelectCommand} with the given {@code index}, and checks that a {@code CommandException}
     * is thrown with the {@code expectedMessage}.
     */
    private void assertExecutionFailure(Index index, String expectedMessage) {
        SelectCommand selectCommand = prepareCommand(index);

        try {
            selectCommand.execute();
            fail("The expected CommandException was not thrown.");
        } catch (CommandException ce) {
            assertEquals(expectedMessage, ce.getMessage());
            assertTrue(eventsCollectorRule.eventsCollector.isEmpty());
        }
    }

    /**
     * Returns a {@code SelectCommand} with parameters {@code index}.
     */
    private SelectCommand prepareCommand(Index index) {
        SelectCommand selectCommand = new SelectCommand(index);
        selectCommand.setData(model, new CommandHistory(), new UndoRedoStack());
        return selectCommand;
    }
}
