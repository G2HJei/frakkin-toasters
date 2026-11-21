package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.RepairEventProcessor.areDamagedVipers;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.RepairEventProcessor.isDamaged;
import static xyz.zlatanov.frakkintoasters.event.action.skillcard.RepairEventProcessor.repairLocation;

public class ResourcesForGalacticaEventProcessor extends QuorumCardEventProcessor<ResourcesForGalacticaEvent> {

    // Any damaged location, Galactica's or Pegasus': the card names no ship.
    public static boolean canPlay(GameView game) {
        return !game.boards().galactica().damagedLocations().isEmpty()
                || !game.boards().pegasus().damagedLocations().isEmpty()
                || !game.boards().galactica().damagedShips().isEmpty();
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.RESOURCES_FOR_GALACTICA;
    }

    @Override
    protected boolean isValidPlay() {
        return event.viperIds() != null
                && (event.location() == null || isDamaged(game, event.location()))
                && areDamagedVipers(game, event.viperIds())
                && (event.location() != null || !event.viperIds().isEmpty());
    }

    @Override
    protected Followup resolve() {
        if (event.location() != null) {
            repairLocation(game, ops, event.location());
        }
        event.viperIds().forEach(id -> ops.ship(id).repair());
        ops.titles().discardPlayedQuorumCard(card());
        return NONE;
    }
}
