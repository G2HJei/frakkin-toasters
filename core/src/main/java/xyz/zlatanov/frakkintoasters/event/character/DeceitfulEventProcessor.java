package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DECEITFUL;

public class DeceitfulEventProcessor extends EventProcessor<DeceitfulEvent> {

    @Override
    protected boolean isValid() {
        return player.hasAbility(DECEITFUL) && event.discard() != null && event.count() >= 0;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(DECEITFUL);
        // What is left of it is still a forced discard, which "For Love" may make smaller again.
        return ForcedDiscards.oneFewer(game, player.number(), event.discard(), event.count());
    }
}
