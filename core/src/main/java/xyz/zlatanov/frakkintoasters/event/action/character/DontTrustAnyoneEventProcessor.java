package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class DontTrustAnyoneEventProcessor extends CharacterActionEventProcessor<DontTrustAnyoneEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.DONT_TRUST_ANYONE;
    }

    static final int CARDS = 2;

    public static boolean canUse(GameView game, PlayerView player) {
        return superCrisisCardsLeft(game) > 0;
    }

    @Override
    protected Followup resolve() {
        val cards = Math.min(CARDS, superCrisisCardsLeft(game));
        for (int i = 0; i < cards; i++) {
            ops.player(event.playerNumber()).drawSuperCrisisCard();
        }
        return NONE;
    }

    // The draw pile and the discard pile, which is shuffled in when the draw pile runs out.
    private static int superCrisisCardsLeft(GameView game) {
        val deck = game.decks().deck(DeckId.SUPER_CRISIS);
        return deck.size() + deck.discardSize();
    }
}
