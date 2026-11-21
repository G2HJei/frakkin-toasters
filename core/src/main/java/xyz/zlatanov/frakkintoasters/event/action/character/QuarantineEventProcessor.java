package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class QuarantineEventProcessor extends CharacterActionEventProcessor<QuarantineEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.QUARANTINE;
    }

    public static boolean canUse(GameView game, PlayerView player) {
        return !game.boards().galactica().shipsInSpace(CivilianShip.class).isEmpty();
    }

    @Override
    protected Followup resolve() {
        val ships = galacticaBoard.shipsInSpace(CivilianShip.class);
        ships.forEach(ship -> ops.player(event.playerNumber()).lookAtShip(ship.id()));
        return one(ships.stream()
                .map(ship -> new QuarantineChoiceEvent(event.playerNumber(), ship.id()))
                .toArray(Event[]::new));
    }
}
