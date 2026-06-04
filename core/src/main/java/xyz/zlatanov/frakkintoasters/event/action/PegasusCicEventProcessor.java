package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class PegasusCicEventProcessor extends EventProcessor<PegasusCicEvent> {

    @Override
    public Followup process() {
        val roll = rollDie();
        if (roll <= 3) {
            return single(new DamagePegasusEvent());
        } else if (roll <= 6) {
            damageBasestar();
            return Followup.NONE;
        } else {
            damageBasestar();
            damageBasestar();
            return Followup.NONE;
        }
    }

    private void damageBasestar() {
        val basestar = findBasestar();
        if (basestar == null) {
            return;
        }
        val damage = game.decks().basestarDamage().draw();
        basestar.damage(damage);
        if (countHits(basestar) > 2) {
            game.destroy(basestar);
        }
    }

    private Basestar findBasestar() {
        return game.boards().galactica().shipsInSpace(Basestar.class)
                .stream()
                .filter(s -> s.id() == event.basestarId())
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
