package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public record PegasusCicActionEvent() implements Event {

    @Override
    public List<Followup> apply(Game game) {
        val roll = game.die().roll();
        if (roll <= 3) {
            return followWith(new DamagePegasusEvent());
        } else if (roll <= 6) {
            return followWith(one(
                    new DamagePegasusEvent(),
                    new DamageGalacticaEvent()));
        } else {
            return followWith(all(
                    new DamageBasestarEvent(),
                    new DamageBasestarEvent()));
        }
    }
}
