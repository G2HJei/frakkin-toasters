package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DECEITFUL;

public class DeceitfulEventProcessor extends EventProcessor<DeceitfulEvent> {

    @Override
    protected boolean isValid() {
        return event.discard() != null && event.count() >= 0;
    }

    @Override
    public Followup process() {
        if (!player.hasAbility(DECEITFUL)) {
            // Romo Lampkin was executed before the discard came, e.g. by the same "A Guilty Verdict": the new character
            // discards in full.
            return single(ForcedDiscards.discard(game, player.number(), event.discard(), event.count()));
        }
        ops.player(player.number()).useAbility(DECEITFUL);
        // What is left of it is still a forced discard, which "For Love" may make smaller again.
        return ForcedDiscards.oneFewer(game, player.number(), event.discard(), event.count());
    }
}
