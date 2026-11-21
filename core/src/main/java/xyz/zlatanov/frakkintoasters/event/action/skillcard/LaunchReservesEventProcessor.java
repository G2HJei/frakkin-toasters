package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.ViperActivationRoundEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_RESERVES;

public class LaunchReservesEventProcessor extends SkillCardEventProcessor<LaunchReservesEvent> {

    // Assault raptors count as vipers (rule 11.3.2.2).
    private static final Set<ShipType> FIGHTERS = Set.of(VIPER, VIPER_MARK_VII, ASSAULT_RAPTOR);

    public static boolean canPlay(GameView game) {
        val galactica = game.boards().galactica();
        return galactica.reserves().stream().anyMatch(HumanFighterView.class::isInstance)
                && galactica.shipsInSpace(HumanFighterView.class).stream().anyMatch(fighter -> fighter.pilot() != null);
    }

    @Override
    public SkillCardType type() {
        return LAUNCH_RESERVES;
    }

    @Override
    protected boolean isValidPlay() {
        val types = event.shipTypes();
        return hasPilotedViper(event.area())
                && types != null && !types.isEmpty() && types.size() <= 2
                && types.stream().allMatch(type -> FIGHTERS.contains(type) && inReserves(type) >= Collections.frequency(types, type));
    }

    // Each launched viper is activated once.
    @Override
    protected Followup resolve() {
        val rounds = new ArrayList<Event>();
        for (val type : event.shipTypes()) {
            int viperId = ops.galactica().launchHumanFighter(type, event.area(), null);
            rounds.add(new ViperActivationRoundEvent(player.number(), viperId, 1, false));
        }
        return all(rounds.toArray(Event[]::new));
    }

    private boolean hasPilotedViper(Location area) {
        return area != null
                && LocationsArea.GALACTICA_SPACE.locations().contains(area)
                && galacticaBoard.shipsIn(area, HumanFighterView.class).stream().anyMatch(fighter -> fighter.pilot() != null);
    }

    private long inReserves(ShipType type) {
        return galacticaBoard.reserves().stream().filter(ship -> ship.getClass() == type.shipClass()).count();
    }
}
