package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.CRITICAL_HIT;

public class DamageBasestarEventProcessor extends EventProcessor<DamageBasestarEvent> {

    private static final int DAMAGE_TO_DESTROY = 3;

    @Override
    public Followup process() {
        if (basestar().isEmpty()) {
            return NONE; // the first of two damages destroyed it, e.g. at "Pegasus CIC"
        }
        ops.ship(event.basestarId()).damage();
        if (damage(basestar().orElseThrow()) < DAMAGE_TO_DESTROY) {
            return NONE;
        }
        ops.ship(event.basestarId()).destroy();
        return single(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed()));
    }

    private Optional<BasestarView> basestar() {
        return galacticaBoard.shipsInSpace(BasestarView.class).stream()
                .filter(basestar -> basestar.id() == event.basestarId())
                .findFirst();
    }

    private static int damage(BasestarView basestar) {
        val tokens = basestar.damage();
        return tokens.size() + (tokens.contains(CRITICAL_HIT) ? 1 : 0); // a critical hit counts as two (rule 11.7.3)
    }
}
