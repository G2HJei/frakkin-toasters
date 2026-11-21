package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyHeavyRaiderEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.Comparator;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.DestroyRaidersEventProcessor.destroyRaidersIn;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public class CivilianSelfDefenseEventProcessor extends QuorumCardEventProcessor<CivilianSelfDefenseEvent> {

    private static final int RAIDERS = 3;

    // A civilian ship with a raider or a heavy raider beside it: otherwise only the roll would happen.
    public static boolean canPlay(GameView game) {
        val board = game.boards().galactica();
        return board.shipsInSpace().entrySet().stream()
                .filter(entry -> entry.getKey() instanceof CivilianShip)
                .anyMatch(entry -> !board.shipsIn(entry.getValue(), Raider.class).isEmpty()
                        || !board.shipsIn(entry.getValue(), HeavyRaider.class).isEmpty());
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.CIVILIAN_SELF_DEFENSE;
    }

    @Override
    protected boolean isValidPlay() {
        val area = civilianShipArea(game, event.civilianShipId());
        return area.isPresent()
                && (event.target() == RAIDER || event.target() == HEAVY_RAIDER)
                && !galacticaBoard.shipsIn(area.get(), event.target().shipClass()).isEmpty();
    }

    @Override
    protected Followup resolve() {
        val area = civilianShipArea(game, event.civilianShipId()).orElseThrow();
        val destroy = event.target() == RAIDER
                ? destroyRaidersIn(game, area, RAIDERS)
                : single(new DestroyHeavyRaiderEvent(galacticaBoard.shipsIn(area, HeavyRaider.class).stream()
                .map(Ship::id)
                .min(Comparator.naturalOrder())
                .orElseThrow()));
        return all(destroy, rollDie(new CivilianSelfDefenseResultEvent(event.civilianShipId())));
    }

    static Optional<Location> civilianShipArea(GameView game, int shipId) {
        return game.boards().galactica().shipsInSpace().entrySet().stream()
                .filter(entry -> entry.getKey() instanceof CivilianShip && entry.getKey().id() == shipId)
                .map(java.util.Map.Entry::getValue)
                .findFirst();
    }
}
