package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

public class PresidentTitleActionEventProcessor extends EventProcessor<PresidentTitleActionEvent> {

    // The President keeps the title's abilities in the "Brig" (rule 4.4.1.2). A card has to be left to draw.
    public static boolean canAct(GameView game, PlayerView player) {
        return player.isHuman()
                && player.character() != null
                && player.character() == game.president()
                && game.decks().quorumCardsLeft() > 0;
    }

    @Override
    protected boolean isValid() {
        return canAct(game, player);
    }

    @Override
    public Followup process() {
        ops.titles().drawQuorumCard();
        return Followup.NONE;
    }
}
