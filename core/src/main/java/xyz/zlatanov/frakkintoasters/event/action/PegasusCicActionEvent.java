package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public record PegasusCicActionEvent(int basestarId) implements Event {

    @Override
    public Followup apply(Game game) {
        val roll = game.die().roll();
        if (roll <= 3) {
            return single(new DamagePegasusEvent());
        } else if (roll <= 6) {
            damageBasestar(game);
            return Followup.NONE;
        } else {
            damageBasestar(game);
            damageBasestar(game);
            return Followup.NONE;
        }
    }

    private void damageBasestar(Game game) {
        val basestar = findBasestar(game);
        if (basestar == null) {
            return;
        }
        val damage = game.decks().basestarDamage().draw();
        basestar.damage(damage);
        if (countHits(basestar) > 2) {
            game.destroy(basestar);
        }
    }

    private Basestar findBasestar(Game game) {
        return game.boards().galactica().shipsInSpace(Basestar.class)
                .stream()
                .filter(s -> s.id() == basestarId)
                .findFirst()
                .orElse(null);
    }

    private int countHits(Basestar basestar) {
        var hits = 0;
        for (var dmg : basestar.damage()) {
            hits += dmg == BasestarDamage.CRITICAL_HIT ? 2 : 1;
        }
        return hits;
    }
}
