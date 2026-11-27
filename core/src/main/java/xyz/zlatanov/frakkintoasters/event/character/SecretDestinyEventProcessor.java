package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.CrisisCardReveals;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SECRET_DESTINY;

public class SecretDestinyEventProcessor extends EventProcessor<SecretDestinyEvent> {

    public static boolean canUse(GameView game, PlayerView player) {
        return CharacterAbilities.canUseMiracle(game, player, SECRET_DESTINY) && game.decks().crisisCardsLeft() > 0;
    }

    @Override
    protected boolean isValid() {
        val cards = game.turn().crisisCards();
        // the card just revealed is the turn's last
        return canUse(game, player) && !cards.isEmpty() && cards.getLast() == event.card();
    }

    @Override
    public Followup process() {
        val playerOps = ops.player(player.number());
        playerOps.useAbility(SECRET_DESTINY);
        ops.discardRevealedCrisisCard(event.card());
        val card = playerOps.drawCrisisCard();
        ops.playCrisisCard(card);
        return CrisisCardReveals.revealed(game, card);
    }
}
