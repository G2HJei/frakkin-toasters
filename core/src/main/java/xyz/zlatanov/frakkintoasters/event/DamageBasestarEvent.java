package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.List;

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
            basestars.getFirst().damage(damage);
        }

        return List.of();
    }
}
