package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakeCagEvent;
import xyz.zlatanov.frakkintoasters.state.Title;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEventProcessor.canBecomeAdmiral;
import static xyz.zlatanov.frakkintoasters.event.officials.MakeCagEventProcessor.canBecomeCag;

public class PresidentialOrderEventProcessor extends QuorumCardEventProcessor<PresidentialOrderEvent> {

    // Any player, the President too, who can take the title.
    public static boolean canPlay(GameView game) {
        return game.players().stream().map(PlayerView::character)
                .anyMatch(c -> canBecomeAdmiral(game, c) || canBecomeCag(game, c));
    }

    @Override
    public QuorumCard card() {
        return QuorumCard.PRESIDENTIAL_ORDER;
    }

    @Override
    protected boolean isValidPlay() {
        if (!isPlayer(event.targetPlayerNumber())) {
            return false;
        }
        val character = game.player(event.targetPlayerNumber()).character();
        return switch (event.title()) {
            case ADMIRAL -> canBecomeAdmiral(game, character);
            case CAG -> canBecomeCag(game, character);
            case PRESIDENT -> false;
            case null -> false;
        };
    }

    @Override
    protected Followup resolve() {
        val character = game.player(event.targetPlayerNumber()).character();
        return all(event.title() == Title.ADMIRAL ? new MakeAdmiralEvent(character) : new MakeCagEvent(character),
                thenDiscard());
    }
}
