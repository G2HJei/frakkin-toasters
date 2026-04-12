package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.player.DistributeBasestarDamageEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public record DamageBasestarEvent() implements Event {

    @Override
    public List<Followup> apply(Game game) {
        val dmgDeck = game.decks().basestarDamage();
        val damage = dmgDeck.draw();

        val basestars = getBasestars(game);

        if (basestars.size() == 1) {
            val basestar = basestars.getFirst();
            basestar.damage(damage);
            if (countHits(basestar) > 2) {
                game.destroy(basestar);
            }
        } else if (basestars.size() > 1) {
            return followWith(
                    new DistributeBasestarDamageEvent(game.currentPlayer(), damage));
        }

        return List.of();
    }

    private List<Basestar> getBasestars(Game game) {
        return game.boards().galactica().shipsInSpace().keySet().stream()
                .filter(s -> s.type() == BASESTAR)
                .map(Basestar.class::cast)
                .toList();
    }

    private int countHits(Basestar basestar) {
        int hits = 0;
        for (var dmg : basestar.damage()) {
            hits += dmg == BasestarDamage.CRITICAL_HIT ? 2 : 1;
        }
        return hits;
    }
}
