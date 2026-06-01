package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;

public class DamagePegasusEventProcessor extends EventProcessor<DamagePegasusEvent> {
    @Override
    public Followup process() {
        val dmgDeck = game.decks().pegasusDamage();
        val pegasusDamage = dmgDeck.draw();
        val damagedLocation = Location.valueOf(pegasusDamage.name());
        game.damage(damagedLocation);
        return Followup.NONE;
    }
}
