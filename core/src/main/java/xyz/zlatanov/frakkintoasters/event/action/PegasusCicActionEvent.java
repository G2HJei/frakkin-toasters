package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FOOD;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FUEL;

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
        val damagedLocation = Location.valueOf(pegasusDamage.name());
        game.damage(damagedLocation);
    }

    private static void damageGalactica(Game game) {
        val dmgDeck = game.decks().galacticaDamage();
        val galacticaDamage = dmgDeck.draw();
        if (FUEL == galacticaDamage) {
            dmgDeck.add(galacticaDamage).shuffle();
            game.boards().galactica().decreaseFuel();
        } else if (FOOD == galacticaDamage) {
            dmgDeck.add(galacticaDamage).shuffle();
            game.boards().galactica().decreaseFood();
        } else {
            val damagedLocation = Location.valueOf(galacticaDamage.name());
            game.damage(damagedLocation);
        }
    }

    private static void damageBasestar(Game game) {

    }
}
