package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.player.DistributeBasestarDamageEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;

public record DamageBasestarEvent() implements Event {

    @Override
    public List<Followup> apply(Game game) {
        val dmgDeck = game.decks().basestarDamage();
        val damage = dmgDeck.draw();

        val basestars = game.boards().galactica().shipsInSpace().keySet().stream()
                .filter(s -> s.type() == ShipType.BASESTAR)
                .map(Basestar.class::cast)
                .toList();

        if (basestars.size() == 1) {
            val basestar = basestars.getFirst();
            basestar.damage(damage);
            if (countHits(basestar) >= 3) {
                destroyBasestar(game, basestar, damage);
            }
        } else {
            return followWith(
                    new DistributeBasestarDamageEvent(game.currentPlayer(), damage));
        }

        return List.of();
    }

    private int countHits(Basestar basestar) {
        int hits = 0;
        for (var dmg : basestar.damage()) {
            hits += dmg == BasestarDamage.CRITICAL_HIT ? 2 : 1;
        }
        return hits;
    }

    private void destroyBasestar(Game game, Basestar basestar, BasestarDamage drawnDamage) {
        game.boards().galactica().remove(basestar);
        game.cylonShips().removed(ShipType.BASESTAR);
        game.decks().discard(drawnDamage);
        basestar.damage().forEach(d -> game.decks().discard(d));
    }
}
