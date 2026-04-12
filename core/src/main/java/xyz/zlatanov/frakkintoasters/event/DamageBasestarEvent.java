package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.List;

public record DamageBasestarEvent() implements Event {
    @Override
    public List<Followup> apply(Game game) {
        ((Basestar) game.boards().galactica().shipsIn(Location.GALACTICA_SPACE_2_OCLOCK).getFirst()).damage().add(BasestarDamage.CRITICAL_HIT);
        return List.of();
    }
}
