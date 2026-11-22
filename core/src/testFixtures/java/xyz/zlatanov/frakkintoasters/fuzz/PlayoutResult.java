package xyz.zlatanov.frakkintoasters.fuzz;

import xyz.zlatanov.frakkintoasters.fuzz.Endings.Ending;
import xyz.zlatanov.frakkintoasters.state.Team;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * One random game.
 *
 * @param rounds     the player turns played, divided by the players
 * @param stoppedBy  the finding that ended the game before its end, {@code null} when it ran to its end
 * @param findings   every finding of the game once, the one that stopped it included, in the order they came
 * @param inputs     the inputs given, the last ones unless the whole trace was asked for
 * @param eventTypes the simple names of the events that ran
 */
public record PlayoutResult(int players,
                            long seed,
                            Optional<Team> winner,
                            Optional<Ending> ending,
                            int rounds,
                            int inputCount,
                            Finding stoppedBy,
                            List<Finding> findings,
                            List<String> inputs,
                            Set<String> eventTypes) {

    public boolean finished() {
        return stoppedBy == null && winner.isPresent();
    }
}
