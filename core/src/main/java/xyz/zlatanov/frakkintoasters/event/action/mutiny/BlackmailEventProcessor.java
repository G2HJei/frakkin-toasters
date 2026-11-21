package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class BlackmailEventProcessor extends MutinyCardEventProcessor<BlackmailEvent> {

    static final int CARDS_TAKEN = 3;

    public static boolean canPlay(GameView game, PlayerView player) {
        return player.character() != game.president()
                && game.player(game.playerNumber(game.president())).skillCards().size() >= CARDS_TAKEN;
    }

    @Override
    public MutinyCard card() {
        return MutinyCard.BLACKMAIL;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game, player);
    }

    @Override
    protected Followup resolve() {
        ops.player(event.playerNumber()).takeRandomSkillCards(game.playerNumber(game.president()), CARDS_TAKEN);
        return single(thenDiscard());
    }
}
