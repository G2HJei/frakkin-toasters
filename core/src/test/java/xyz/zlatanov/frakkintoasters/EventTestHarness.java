package xyz.zlatanov.frakkintoasters;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.Followup.skillCheckFollowup;


public abstract class EventTestHarness<E extends Event> extends TestHarness {

    private EventProcessor<E> eventProcessor;
    private Followup          followup;
    private boolean           followupAsserted;

    @Override
    @BeforeEach
    protected void setUpGame() {
        super.setUpGame();
        createEventProcessor();
    }

    @AfterEach
    protected void assertNoFollowUpByDefault() {
        if (!followupAsserted) {
            assertEquals(Followup.NONE, followup);
        }
    }

    /* Event execution utility methods */
    protected void assertFollowup(Followup expected) {
        followupAsserted = true;
        assertEquals(expected, followup);
    }

    protected void execute(E event) {
        followup = eventProcessor.execute(game, event);
    }

    protected void assertFollowup(Event expected) {
        followupAsserted = true;
        assertEquals(single(expected), followup);
    }

    protected void assertInvalid(E event) {
        followupAsserted = true;
        assertThrows(InvalidActionException.class, () -> execute(event));
    }

    protected void assertSkillCheckTriggered() {
        followupAsserted = true;
        assertEquals(skillCheckFollowup(game), followup);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void createEventProcessor() {
        val testClassName = getClass().getName();
        try {
            val processorClassName = testClassName.substring(0, testClassName.length() - "Test".length());
            val processorClass = Class.forName(processorClassName);
            eventProcessor = (EventProcessor<E>) processorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Test class name does not match processor name/package", e);
        }
    }
}
