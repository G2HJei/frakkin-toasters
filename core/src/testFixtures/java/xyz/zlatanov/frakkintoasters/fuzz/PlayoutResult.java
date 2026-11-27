package xyz.zlatanov.frakkintoasters.fuzz;

import xyz.zlatanov.frakkintoasters.fuzz.Endings.Ending;
import xyz.zlatanov.frakkintoasters.state.Team;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @param rounds the player turns played, divided by the players
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

    public boolean failed() {
        return stoppedBy != null;
    }

    // What a run of thousands of games keeps of each: no inputs, no event types, no pauses.
    PlayoutResult slim() {
        return new PlayoutResult(players, seed, winner, ending, rounds, inputCount,
                stoppedBy == null ? null : stoppedBy.withoutPause(),
                findings.stream().map(Finding::withoutPause).toList(), List.of(), Set.of());
    }
}
