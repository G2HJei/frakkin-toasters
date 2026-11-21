package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SLEEPER_AGENT;

public class SleeperAgentEventProcessor extends EventProcessor<SleeperAgentEvent> {

    @Override
    protected boolean isValid() {
        return player.hasAbility(SLEEPER_AGENT);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(SLEEPER_AGENT);
        // Her cards may have sent her there already, e.g. a second Mutiny Card with the Mutineer card.
        return canBeSentToBrig(game, player.number()) ? single(new SendToBrigEvent(player.number())) : NONE;
    }
}
