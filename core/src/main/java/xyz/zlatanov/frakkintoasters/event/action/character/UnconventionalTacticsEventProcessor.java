package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;

public class UnconventionalTacticsEventProcessor extends CharacterActionEventProcessor<UnconventionalTacticsEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.UNCONVENTIONAL_TACTICS;
    }

    public static boolean canUse(GameView game, PlayerView player) {
        return true;
    }

    @Override
    protected boolean isValidUse() {
        return event.resource() != null && event.resource() != POPULATION;
    }

    @Override
    protected Followup resolve() {
        return all(new LoseResourceEvent(POPULATION, 1), new GainResourceEvent(event.resource(), 1));
    }
}
