package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;

public record PegasusCicActionEvent() implements Event {

    @Override
    public void apply(Game game) {
        val roll = game.die().roll();
        if (roll <= 3) {
            damagePegasus(game);
        } else if (roll <= 6) {
            damageGalactica(game);
        } else {
            //damage basestar
        }
    }

    private static void damagePegasus(Game game) {
        val dmgDeck = game.decks().pegasusDamage();
        val pegasusDamage = dmgDeck.draw();
        dmgDeck.add(pegasusDamage).shuffle();
        val damagedLocation = Location.valueOf(pegasusDamage.name());
        game.damage(damagedLocation);
    }

    private static void damageGalactica(Game game) {

    }

    private static void damageBasestar(Game game) {
        
    }
}
