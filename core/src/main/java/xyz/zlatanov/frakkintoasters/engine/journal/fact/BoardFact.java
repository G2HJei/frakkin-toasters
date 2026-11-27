package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;

public sealed interface BoardFact extends Fact {

    record ResourceDecreased(Resource resource, int amount) implements BoardFact {
    }

    record ResourceIncreased(Resource resource, int amount) implements BoardFact {
    }

    record JumpPreparationIncreased() implements BoardFact {
    }

    record JumpPreparationDecreased() implements BoardFact {
    }

    // The fleet token goes back to the start space when the fleet jumps (rule 12.1.1).
    record JumpPreparationReset() implements BoardFact {
    }

    record LocationDamaged(Location location) implements BoardFact {
    }

    // The damage token goes back to its pile with other facts (rule 11.7.2.2).
    record LocationRepaired(Location location) implements BoardFact {
    }

    record BoardingPartyAdvanced() implements BoardFact {
    }

    record PursuitAdvanced() implements BoardFact {
    }

    record PursuitReset() implements BoardFact {
    }

    record EngineRoomActivated() implements BoardFact {
    }

    // All four Pegasus locations are damaged: the board leaves the game (rule 11.7.2.4). The characters on it go to
    // "Sickbay" with their own facts.
    record PegasusDestroyed() implements BoardFact {
    }
}
