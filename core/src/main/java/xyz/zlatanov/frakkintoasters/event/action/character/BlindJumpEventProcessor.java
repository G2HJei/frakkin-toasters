package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class BlindJumpEventProcessor extends CharacterActionEventProcessor<BlindJumpEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.BLIND_JUMP;
    }

    static final int MAX_DISTANCE    = 6;
    static final int CIVILIAN_SHIPS = 2;

    public static boolean canUse(GameView game, PlayerView player) {
        return game.distanceTraveled() <= MAX_DISTANCE;
    }

    // The jump ignores the jump preparation track, so the red spaces cost no population.
    @Override
    protected Followup resolve() {
        return all(new DrawAndDestroyCivilianShipsEvent(CIVILIAN_SHIPS), new JumpingTheFleetEvent(true));
    }
}
