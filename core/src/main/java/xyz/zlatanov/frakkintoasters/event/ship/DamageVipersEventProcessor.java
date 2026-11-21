package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.RESERVES;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.SPACE;

public class DamageVipersEventProcessor extends EventProcessor<DamageVipersEvent> {

    @Override
    public Followup process() {
        val candidates = candidates(game, event.area());
        if (event.count() <= 0 || candidates.isEmpty()) {
            return NONE;
        }
        // One viper at a time. The player taking an action chooses, the current player when nobody is (rule 11.3.2.2).
        // Fighters of the same kind in the same place are alike, so only the first of each is an option, and a single
        // kind needs no choice.
        val chooser = game.actingPlayer().number();
        val options = alike(candidates).stream()
                .map(fighter -> new DamageChosenViperEvent(chooser, fighter.id()))
                .toArray(Event[]::new);
        val rest = new DamageVipersEvent(event.count() - 1, event.area());
        return all(one(options), single(rest));
    }

    /**
     * @return the fighters a "damage vipers" effect can pick in the area: vipers and Mark VIIs, and assault raptors, which
     * count as vipers (rule 11.3.2.2). The "Reserves" first, then space
     */
    static List<HumanFighterView> candidates(GameView game, DamageVipersEvent.Area area) {
        val galactica = game.boards().galactica();
        Stream<HumanFighterView> inReserves = area == SPACE ? Stream.empty() : galactica.reserves().stream()
                .filter(HumanFighterView.class::isInstance)
                .map(HumanFighterView.class::cast);
        Stream<HumanFighterView> inSpace = area == RESERVES ? Stream.empty()
                : galactica.shipsInSpace(HumanFighterView.class).stream();
        return Stream.concat(inReserves, inSpace)
                .filter(fighter -> fighter instanceof Viper || fighter instanceof AssaultRaptor)
                .toList();
    }

    private List<HumanFighterView> alike(List<HumanFighterView> candidates) {
        val galactica = game.boards().galactica();
        val firstOfEachKind = new LinkedHashMap<Kind, HumanFighterView>();
        for (val fighter : candidates) {
            val inReserves = galactica.reserves().stream().anyMatch(ship -> ship.id() == fighter.id());
            val kind = new Kind(fighter.getClass(), inReserves ? null : galactica.locate(fighter), fighter.pilot());
            firstOfEachKind.putIfAbsent(kind, fighter);
        }
        return List.copyOf(firstOfEachKind.values());
    }

    private record Kind(Class<?> type, Location location, Character pilot) {
    }
}
