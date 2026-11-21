package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.GLIMPSE_THE_FACE_OF_GOD;

public class GlimpseTheFaceOfGodEventProcessor extends CharacterMovementEventProcessor<GlimpseTheFaceOfGodEvent> {

    static final int CARDS = 2;

    // An empty Destiny deck is built anew, so there is always a card to take.
    public static boolean canUse(GameView game, PlayerView player) {
        return true;
    }

    @Override
    public CharacterAbility ability() {
        return GLIMPSE_THE_FACE_OF_GOD;
    }

    @Override
    protected Followup resolve() {
        ops.player(player.number()).drawFromDestinyDeck(CARDS);
        return single(new PlayerDecisionEvent<>(player.number(), GlimpseTheFaceOfGodPlaceEvent.class));
    }
}
