package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.CrisisCardReveals;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RELIGIOUS_VISIONS;

public class ReligiousVisionsEventProcessor extends EventProcessor<ReligiousVisionsEvent> {

    public static boolean drawsTwo(GameView game, PlayerView player) {
        return player.hasAbility(RELIGIOUS_VISIONS) && game.decks().crisisCardsLeft() >= 2;
    }

    @Override
    protected boolean isValid() {
        return player.hasAbility(RELIGIOUS_VISIONS)
                && event.crisisCardToPlay() != null
                && event.crisisCardToPutAtBottom() != null;
    }

    @Override
    public Followup process() {
        val playerOps = ops.player(player.number());
        playerOps.useAbility(RELIGIOUS_VISIONS);
        playerOps.placeCrisisCardOnBottom(event.crisisCardToPutAtBottom());
        // The card she keeps stays with the turn, as the one the Crisis step draws does (rule 8.8).
        ops.playCrisisCard(event.crisisCardToPlay());
        return CrisisCardReveals.drawn(game, event.crisisCardToPlay(), player.number());
    }
}
