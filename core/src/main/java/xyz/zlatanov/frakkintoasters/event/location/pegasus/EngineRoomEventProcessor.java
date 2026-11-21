package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.player.TreacheryDiscards;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.List;
import java.util.Arrays;

public class EngineRoomEventProcessor extends LocationActionProcessor<EngineRoomEvent> {

    public static boolean canAct(PlayerView player) {
        return player.skillCards().size() >= 2;
    }

    @Override
    protected boolean isValidAction() {
        return holdsSkillCards(Arrays.asList(event.discardCard1(), event.discardCard2()));
    }

    @Override
    protected Followup act() {
        ops.player(player.number()).discard(event.discardCard1(), event.discardCard2());
        ops.pegasus().activateEngineRoom();
        return TreacheryDiscards.mutinyCardFor(game, ops, player.number(), List.of(event.discardCard1(), event.discardCard2()));
    }
}
