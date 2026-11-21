package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.LOGIC_BOUND;

public class LogicBoundEventProcessor extends EventProcessor<LogicBoundEvent> {

    @Override
    protected boolean isValid() {
        val skillCheck = game.activeSkillCheck();
        // The card is one of those he just put in, face down on the pile of the check.
        return skillCheck != null
                && skillCheck.result() == null
                && player.hasAbility(LOGIC_BOUND)
                && event.card() != null
                && skillCheck.cards().cards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(LOGIC_BOUND);
        // Like "Investigative Committee", everyone learns he put this card in.
        ops.skillCheck().show(player.number(), List.of(event.card()));
        return NONE;
    }
}
