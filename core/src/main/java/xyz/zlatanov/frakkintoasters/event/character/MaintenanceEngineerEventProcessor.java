package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.MAINTENANCE_ENGINEER;

public class MaintenanceEngineerEventProcessor extends EventProcessor<MaintenanceEngineerEvent> {

    public static boolean canUse(GameView game, PlayerView player) {
        return player.number() == game.turn().currentPlayerNumber()
                && CharacterAbilities.canUseOncePerTurn(game, player, CharacterAbility.MAINTENANCE_ENGINEER);
    }

    public static Followup offer(GameView game, PlayerView player) {
        return canUse(game, player)
                ? one(new MaintenanceEngineerEvent(player.number()), new NoOpEvent(player.number()))
                : NONE;
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(CharacterAbility.MAINTENANCE_ENGINEER);
        return single(new GrantEvent(player.number(), ACTION, MAINTENANCE_ENGINEER));
    }
}
