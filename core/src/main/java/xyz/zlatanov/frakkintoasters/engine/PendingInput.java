package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.Set;

public sealed interface PendingInput {

    int player();

    boolean secret();

    record AwaitingDecision(PlayerDecisionEvent<?> decision) implements PendingInput {

        @Override
        public int player() {
            return decision.playerNumber();
        }

        @Override
        public boolean secret() {
            return false;
        }
    }

    record AwaitingChoice(int player, Set<Followup> options) implements PendingInput {

        @Override
        public boolean secret() {
            return false;
        }
    }
}
