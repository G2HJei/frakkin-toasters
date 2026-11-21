package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.ShuffleCivilianShipIntoDeckEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.damageGalactica;

public class ScavengingForPartsEventProcessor extends MutinyCardEventProcessor<ScavengingForPartsEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.SCAVENGING_FOR_PARTS;
    }

    // "If possible": a civilian ship in space has to be chosen when there is one.
    @Override
    protected boolean isValidPlay() {
        val civilianShips = galacticaBoard.shipsInSpace(CivilianShip.class);
        return event.civilianShipId() == null
                ? civilianShips.isEmpty()
                : civilianShips.stream().anyMatch(ship -> ship.id() == event.civilianShipId());
    }

    @Override
    protected Followup resolve() {
        val civilianShip = event.civilianShipId() == null
                ? NONE
                : single(new ShuffleCivilianShipIntoDeckEvent(event.civilianShipId()));
        return allOf(damageGalactica(), civilianShip, single(thenDiscard()));
    }
}
