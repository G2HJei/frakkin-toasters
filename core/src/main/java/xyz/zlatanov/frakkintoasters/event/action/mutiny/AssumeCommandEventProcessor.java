package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEventProcessor.canBecomeAdmiral;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class AssumeCommandEventProcessor extends MutinyCardEventProcessor<AssumeCommandEvent> {

    static final int DISCARDS = 5;

    public static boolean canPlay(GameView game, PlayerView player) {
        return player.skillCards().size() >= DISCARDS
                && !game.locate(player.character()).equals(Optional.of(BRIG))
                && canBecomeAdmiral(game, player.character());
    }

    @Override
    public MutinyCard card() {
        return MutinyCard.ASSUME_COMMAND;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game, player)
                && event.discards() != null
                && event.discards().size() == DISCARDS
                && holdsSkillCards(event.discards());
    }

    // The discard is a choice, so a "Bait" or a "Dradis Contact" among the cards draws a Mutiny Card.
    @Override
    protected Followup resolve() {
        return all(new DiscardSkillCardsEvent(event.playerNumber(), event.discards()),
                new MakeAdmiralEvent(player.character()),
                thenDiscard());
    }
}
