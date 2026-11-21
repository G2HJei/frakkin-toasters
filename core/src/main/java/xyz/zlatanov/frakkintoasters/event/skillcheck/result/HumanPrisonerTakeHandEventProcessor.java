package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;

import java.util.Objects;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class HumanPrisonerTakeHandEventProcessor extends EventProcessor<HumanPrisonerTakeHandEvent> {

    @Override
    protected boolean isValid() {
        val check = game.activeSkillCheck();
        return check != null && Objects.equals(check.playedBy(), player.number())
                && isPlayer(event.targetPlayerNumber()) && game.player(event.targetPlayerNumber()).isHuman();
    }

    @Override
    public Followup process() {
        val target = game.player(event.targetPlayerNumber());
        ops.player(target.number()).giveSkillCards(player.number(), target.skillCards().cards());
        return single(new SendToSickbayEvent(target.number()));
    }
}
