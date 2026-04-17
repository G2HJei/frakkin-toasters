package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.decisionconstraint.DecisionConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventTest {

    @Test
    void shouldFollowExecutionOrder() {
        new FakeEvent().execute(Game.builder().build(), new FakeDecisionConstraint());
        assertEquals(List.of("validConstraint", "isValid", "apply"), sequence);
    }

    static List<String> sequence = new ArrayList<>();

    static class FakeDecisionConstraint implements DecisionConstraint<FakeEvent> {

        @Override
        public boolean validConstraint(FakeEvent event) {
            sequence.add("validConstraint");
            return true;
        }
    }

    static class FakeEvent implements Event<FakeEvent> {

        @Override
        public boolean isValid(Game game) {
            sequence.add("isValid");
            return true;
        }

        @Override
        public List<Followup> apply(Game game) {
            sequence.add("apply");
            return List.of();
        }

    }
}