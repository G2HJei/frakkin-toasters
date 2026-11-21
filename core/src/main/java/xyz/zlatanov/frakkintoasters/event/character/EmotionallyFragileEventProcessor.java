package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor.canBeExecuted;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.EMOTIONALLY_FRAGILE;

public class EmotionallyFragileEventProcessor extends EventProcessor<EmotionallyFragileEvent> {

    @Override
    protected boolean isValid() {
        return game.turn().triggeredAbilities().getOrDefault(player.number(), Set.of()).contains(EMOTIONALLY_FRAGILE);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).clearAbilityTrigger(EMOTIONALLY_FRAGILE);
        // Not when something already executed her or revealed her in the meantime.
        return player.hasAbility(EMOTIONALLY_FRAGILE) && canBeExecuted(game, player.number())
                ? single(new ExecuteEvent(player.number(), true))
                : NONE;
    }
}
