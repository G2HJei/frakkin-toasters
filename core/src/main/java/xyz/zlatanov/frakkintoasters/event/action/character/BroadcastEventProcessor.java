package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class BroadcastEventProcessor extends CharacterActionEventProcessor<BroadcastEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.BROADCAST;
    }

    static final int GAIN = 2;

    public static boolean canUse(GameView game, PlayerView player) {
        return true;
    }

    @Override
    protected boolean isValidUse() {
        return event.resource() != null;
    }

    @Override
    protected Followup resolve() {
        return single(new GainResourceEvent(event.resource(), GAIN));
    }
}
