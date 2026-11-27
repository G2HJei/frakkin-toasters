package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.optional;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RECON;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;

public class ReconEventProcessor extends EventProcessor<ReconEvent> {

    public static boolean canUse(GameView game, PlayerView player) {
        return player.hasAbility(RECON) && game.decks().crisisCardsLeft() > 0;
    }

    public static Followup offer(GameView game) {
        val current = game.currentPlayer();
        return canUse(game, current) ? optional(current.number(), new ReconEvent(current.number())) : NONE;
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(RECON);
        return single(new LookAtTopCardEvent(player.number(), CRISIS));
    }
}
