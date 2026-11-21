package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.action.ResetActingPlayerEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.ALERT_VIPER_PILOT;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;

public class AlertViperPilotEventProcessor extends EventProcessor<AlertViperPilotEvent> {

    // On a Galactica location other than the "Brig": not a pilot already, nor on Pegasus or Colonial One.
    public static boolean canUse(GameView game, PlayerView player) {
        return player.hasAbility(CharacterAbility.ALERT_VIPER_PILOT)
                && game.locate(player.character())
                .filter(location -> GALACTICA.locations().contains(location) && location != BRIG)
                .isPresent();
    }

    // The viper that was placed, still in space and unmanned.
    public static boolean canPilot(GameView game, int viperId) {
        return game.boards().galactica().shipsInSpace(HumanFighterView.class).stream()
                .anyMatch(fighter -> fighter.id() == viperId && fighter.pilot() == null);
    }

    /**
     * @return right after vipers were placed from the "Reserves", the choice of one to pilot or none, for the player who
     * can use "Alert Viper Pilot", {@link Followup#NONE} when nobody can
     */
    public static Followup offer(GameView game, List<Integer> viperIds) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(apollo -> {
                    val options = new ArrayList<Event>();
                    viperIds.stream()
                            .filter(viperId -> canPilot(game, viperId))
                            .forEach(viperId -> options.add(new AlertViperPilotEvent(apollo.number(), viperId)));
                    if (options.isEmpty()) {
                        return NONE;
                    }
                    options.add(new NoOpEvent(apollo.number()));
                    return one(options.toArray(Event[]::new));
                })
                .orElse(NONE);
    }

    /**
     * @return whether a player has "Alert Viper Pilot", so a launch of several vipers asks after them
     * ({@link OfferAlertViperPilotEvent})
     */
    public static boolean mayBeOffered(GameView game) {
        return game.players().stream()
                .anyMatch(player -> player.hasAbility(CharacterAbility.ALERT_VIPER_PILOT));
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player) && canPilot(game, event.viperId());
    }

    @Override
    public Followup process() {
        int apollo = player.number();
        ops.player(apollo).useAbility(CharacterAbility.ALERT_VIPER_PILOT);
        ops.player(apollo).pilot(event.viperId());
        val action = new GrantEvent(apollo, ACTION, ALERT_VIPER_PILOT);
        // The grant lets him act when it is another player's turn, and the turn goes on as it was afterwards.
        return game.turn().actingPlayerNumber() == apollo
                ? single(action)
                : all(action, new ResetActingPlayerEvent());
    }
}
