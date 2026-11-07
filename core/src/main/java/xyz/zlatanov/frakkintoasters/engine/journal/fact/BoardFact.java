package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

/**
 * Facts about the boards and their tracks.
 */
public sealed interface BoardFact extends Fact {

    record ResourceDecreased(Resource resource, int amount) implements BoardFact {
    }

    record ResourceIncreased(Resource resource, int amount) implements BoardFact {
    }

    record JumpPreparationDecreased() implements BoardFact {
    }

    record LocationDamaged(Location location) implements BoardFact {
    }

    record BoardingPartyAdvanced() implements BoardFact {
    }

    record PursuitAdvanced() implements BoardFact {
    }

    record PursuitReset() implements BoardFact {
    }

    record EngineRoomActivated() implements BoardFact {
    }
}
