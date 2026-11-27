package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Viper;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ESCORT;

public class EscortEventProcessor extends EventProcessor<EscortEvent> {

    /**
     * @return the vipers "Escort" may destroy: undamaged ones, in the "Reserves" (the first of each kind, they are alike)
     * or in space. A Mark VII is a viper
     */
    public static List<Integer> vipers(GameView game) {
        val galactica = game.boards().galactica();
        val firstOfEachKind = new LinkedHashMap<Class<?>, Integer>();
        galactica.reserves().stream()
                .filter(Viper.class::isInstance)
                .forEach(viper -> firstOfEachKind.putIfAbsent(viper.getClass(), viper.id()));
        val vipers = new ArrayList<>(firstOfEachKind.values());
        galactica.shipsInSpace(Viper.class).stream().map(Ship::id).forEach(vipers::add);
        return vipers;
    }

    public static Followup offer(GameView game, int civilianShipId) {
        val vipers = vipers(game);
        if (vipers.isEmpty()) {
            return NONE;
        }
        return game.players().stream()
                .filter(player -> CharacterAbilities.canUseMiracle(game, player, ESCORT))
                .findFirst()
                .map(hotDog -> {
                    val options = new ArrayList<Event>();
                    vipers.forEach(viperId -> options.add(new EscortEvent(hotDog.number(), civilianShipId, viperId)));
                    options.add(new DestroyCivilianShipEvent(civilianShipId, true)); // names nobody: he chooses
                    return one(options.toArray(Event[]::new));
                })
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return CharacterAbilities.canUseMiracle(game, player, ESCORT)
                && galacticaBoard.shipsInSpace(CivilianShip.class).stream().anyMatch(s -> s.id() == event.civilianShipId())
                && vipers(game).contains(event.viperId());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(ESCORT);
        ops.ship(event.viperId()).destroy();
        ops.ship(event.civilianShipId()).escort();
        return NONE;
    }
}
