package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;

public record DamagePegasusEvent() implements Event {
    @Override
    public Followup apply(Game game) {
        val dmgDeck = game.decks().pegasusDamage();
        val pegasusDamage = dmgDeck.draw();
        val damagedLocation = Location.valueOf(pegasusDamage.name());
        game.damage(damagedLocation);
        return Followup.NONE;
    }
}
