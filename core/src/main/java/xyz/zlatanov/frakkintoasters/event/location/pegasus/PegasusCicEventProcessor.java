package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class PegasusCicEventProcessor extends EventProcessor<PegasusCicEvent> {

    @Override
    public Followup process() {
        val roll = ops.rollDie();
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
        ops.ship(basestar.id()).damage();
        if (countHits(basestar) > 2) {
            ops.ship(basestar.id()).destroy();
        }
    }

    private BasestarView findBasestar() {
        return galacticaBoard.shipsInSpace(BasestarView.class)
                .stream()
                .filter(s -> s.id() == event.basestarId())
                .findFirst()
                .orElse(null);
    }

    private int countHits(BasestarView basestar) {
        var hits = 0;
        for (var dmg : basestar.damage()) {
            hits += dmg == BasestarDamage.CRITICAL_HIT ? 2 : 1;
        }
        return hits;
    }
}
