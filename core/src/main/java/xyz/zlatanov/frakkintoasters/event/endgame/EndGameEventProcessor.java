package xyz.zlatanov.frakkintoasters.event.endgame;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.Team.HUMANS;

public class EndGameEventProcessor extends EventProcessor<EndGameEvent> {

    @Override
    public Followup process() {
        // Each unrevealed Personal Goal Loyalty Card of a human player reduces the resource on it by 1. Revealed Cylons
        // don't reduce anything (rule 16.2).
        for (val human : game.players()) {
            if (!human.isHuman()) { // a Cylon Leader holds Motive Cards, not Loyalty Cards
                continue;
            }
            for (val card : human.loyaltyCards().cards()) {
                if (card.personalGoalResource() != null) {
                    ops.galactica().decreaseResource(card.personalGoalResource(), 1);
                }
            }
        }
        // The humans win unless a resource is at 0 afterwards.
        val depleted = Arrays.stream(Resource.values()).anyMatch(resource -> level(resource) <= 0);
        EndGame.end(game, ops, depleted ? CYLONS : HUMANS);
        return NONE;
    }

    private int level(Resource resource) {
        return switch (resource) {
            case FUEL -> galacticaBoard.fuel();
            case FOOD -> galacticaBoard.food();
            case MORALE -> galacticaBoard.morale();
            case POPULATION -> galacticaBoard.population();
        };
    }
}
