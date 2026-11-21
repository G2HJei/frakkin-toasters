package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DeclareMartialLawEventProcessor extends CharacterActionEventProcessor<DeclareMartialLawEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.DECLARE_MARTIAL_LAW;
    }

    public static boolean canUse(GameView game, PlayerView player) {
        return MakePresidentEventProcessor.canBecomePresident(game, game.admiral());
    }

    @Override
    protected boolean isValidUse() {
        return canUse(game, player);
    }

    @Override
    protected Followup resolve() {
        return single(new MakePresidentEvent(game.admiral()));
    }
}
