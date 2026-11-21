package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEventProcessor.isGalacticaDestination;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class MakeADealEventProcessor extends MutinyCardEventProcessor<MakeADealEvent> {

    public static boolean canPlay(GameView game) {
        return !game.boards().galactica().charactersIn(BRIG).isEmpty();
    }

    @Override
    public MutinyCard card() {
        return MutinyCard.MAKE_A_DEAL;
    }

    // The card says any location on Galactica, so a hazardous one ("Sickbay") counts too, the "Brig" not. Whoever draws has to be able to draw Mutiny Cards: a human, or an Infiltrating Cylon Leader (rules
    // 5.2.3.4 and 5.2.3.5), the player himself included.
    @Override
    protected boolean isValidPlay() {
        return isPlayer(event.targetPlayerNumber())
                && galacticaBoard.charactersIn(BRIG).contains(game.player(event.targetPlayerNumber()).character())
                && isGalacticaDestination(event.destination())
                && isPlayer(event.drawerPlayerNumber())
                && game.player(event.drawerPlayerNumber()).isHuman();
    }

    @Override
    protected Followup resolve() {
        return all(new MoveFromBrigEvent(event.targetPlayerNumber(), event.destination()),
                thenDiscard(),
                new DrawMutinyCardEvent(event.drawerPlayerNumber()));
    }
}
