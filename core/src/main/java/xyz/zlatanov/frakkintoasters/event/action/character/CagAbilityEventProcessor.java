package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.canActivateUnmanned;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class CagAbilityEventProcessor extends CharacterActionEventProcessor<CagAbilityEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.CAG;
    }

    static final int ACTIVATIONS = 6;

    public static boolean canUse(GameView game, PlayerView player) {
        return canActivateUnmanned(game);
    }

    @Override
    protected Followup resolve() {
        return single(new UnmannedViperRoundEvent(event.playerNumber(), ACTIVATIONS));
    }
}
