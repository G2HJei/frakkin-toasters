package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.action.quorum.CivilianSelfDefenseEventProcessor.civilianShipArea;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.CIVILIAN_SELF_DEFENSE;

public class CivilianSelfDefenseResultEventProcessor extends EventProcessor<CivilianSelfDefenseResultEvent> {

    private static final int HIGHEST_ROLL_TO_LOSE = 2;

    // The ship may be gone already, e.g. destroyed by a card played on the roll.
    @Override
    public Followup process() {
        ops.titles().discardPlayedQuorumCard(CIVILIAN_SELF_DEFENSE);
        return game.turn().dieRoll().result() <= HIGHEST_ROLL_TO_LOSE
                && civilianShipArea(game, event.civilianShipId()).isPresent()
                ? single(new DestroyCivilianShipEvent(event.civilianShipId()))
                : NONE;
    }
}
