package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.Arrays;
import java.util.List;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.event.character.CharacterAbilities;
import xyz.zlatanov.frakkintoasters.event.player.ReturnToResurrectionShipEventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.METICULOUS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;

public class MeticulousEventProcessor extends EventProcessor<MeticulousEvent> {

    /**
     * @return where an Infiltrating Aaron Doral may go with "Meticulous": a Cylon location other than the "Resurrection
     * Ship"
     */
    public static List<Location> destinations() {
        return Arrays.stream(Location.values())
                .filter(location -> location.isCylonLocation() && !location.isSpaceLocation() && location != RESURRECTION_SHIP)
                .toList();
    }

    @Override
    protected boolean isValid() {
        return player.isInfiltrating() && event.location() != null
                && (event.location() == RESURRECTION_SHIP
                || destinations().contains(event.location())
                && CharacterAbilities.canUseMiracle(game, player, CharacterAbility.METICULOUS));
    }

    @Override
    public Followup process() {
        val meticulous = event.location() != RESURRECTION_SHIP;
        if (meticulous) {
            ops.player(event.playerNumber()).useAbility(CharacterAbility.METICULOUS);
        }
        val discard = ReturnToResurrectionShipEventProcessor.endInfiltration(game, ops, player, event.location());
        return allOf(discard, meticulous ? single(new GrantEvent(event.playerNumber(), ACTION, METICULOUS)) : NONE);
    }
}
