package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.ReturnVipersToReservesEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;

public class ClippedWingsEventProcessor extends MutinyCardEventProcessor<ClippedWingsEvent> {

    // Mark VIIs and assault raptors count as vipers (rules 11.3.1.1 and 11.3.2.2).
    public static boolean canPlay(GameView game) {
        return !game.boards().galactica().shipsInSpace(HumanFighterView.class).isEmpty();
    }

    @Override
    public MutinyCard card() {
        return MutinyCard.CLIPPED_WINGS;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game);
    }

    // The vipers return with ReturnVipersToReservesEvent, which lands their pilots on the "Hangar Deck".
    @Override
    protected Followup resolve() {
        galacticaBoard.damagedShips().stream().map(Ship::id).forEach(id -> ops.ship(id).repair());
        return all(new ReturnVipersToReservesEvent(), treachery(event.playerNumber(), 2), thenDiscard());
    }
}
