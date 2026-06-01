package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FOOD;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FUEL;

public class DamageGalacticaEventProcessor extends EventProcessor<DamageGalacticaEvent> {

    @Override
    public Followup process() {
        val dmgDeck = game.decks().galacticaDamage();
        val galacticaDamage = dmgDeck.draw();
        if (FUEL == galacticaDamage) {
            game.boards().galactica().decreaseFuel(1);
        } else if (FOOD == galacticaDamage) {
            game.boards().galactica().decreaseFood(1);
        } else {
            val damagedLocation = Location.valueOf(galacticaDamage.name());
            game.damage(damagedLocation);
        }
        return Followup.NONE;
    }
}
