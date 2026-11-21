package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.character.DutifulEventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.MoveCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMUNICATIONS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.EFFICIENT;
import static java.util.stream.Collectors.toSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class CommunicationsEventProcessor extends LocationActionProcessor<CommunicationsEvent> {

    // 2 different civilian ships in space, or all of them when fewer are left. Dee looks at all of them, so her choice
    // doesn't matter ("Efficient").
    @Override
    protected boolean isValidAction() {
        val inSpace = galacticaBoard.shipsInSpace(CivilianShip.class).stream().map(Ship::id).collect(toSet());
        val chosen = Stream.of(event.civilianShipId1(), event.civilianShipId2()).filter(Objects::nonNull).toList();
        return inSpace.containsAll(chosen)
                && new HashSet<>(chosen).size() == chosen.size()
                && (player.hasAbility(EFFICIENT) || chosen.size() == Math.min(2, inSpace.size()));
    }

    @Override
    protected Followup act() {
        val moves = all(shipsLookedAt().stream()
                .peek(this::revealCivilianShip)
                .map(this::moveFollowups)
                .toArray(Followup[]::new));
        return allOf(moves, DutifulEventProcessor.after(game, player, COMMUNICATIONS)); // Louis Hoshi's "Dutiful"
    }

    // Anastasia "Dee" Dualla's "Efficient": every civilian ship on the board, and she may move any number of them.
    private List<Integer> shipsLookedAt() {
        if (!player.hasAbility(EFFICIENT)) {
            return Stream.of(event.civilianShipId1(), event.civilianShipId2()).filter(Objects::nonNull).toList();
        }
        val all = galacticaBoard.shipsInSpace(CivilianShip.class).stream().map(Ship::id).sorted().toList();
        if (!all.isEmpty()) {
            ops.player(player.number()).useAbility(EFFICIENT);
        }
        return all;
    }

    private void revealCivilianShip(Integer civId) {
        ops.player(player.number()).lookAtShip(civId);
    }

    private Followup moveFollowups(int civilianShipId) {
        val civShip = galacticaBoard.shipInSpace(civilianShipId, CivilianShip.class);
        val location = galacticaBoard.locate(civShip);
        val adjacentLocations = location.adjacentLocations();
        return one(
                new NoOpEvent(player.number()),
                new MoveCivilianShipEvent(civilianShipId, adjacentLocations.getFirst()),
                new MoveCivilianShipEvent(civilianShipId, adjacentLocations.getLast()));
    }
}
