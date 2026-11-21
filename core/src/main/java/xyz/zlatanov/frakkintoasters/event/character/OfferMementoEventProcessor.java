package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MEMENTO;

public class OfferMementoEventProcessor extends EventProcessor<OfferMementoEvent> {

    @Override
    protected boolean isValid() {
        return game.turn().triggeredAbilities().getOrDefault(player.number(), Set.of()).contains(MEMENTO);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).clearAbilityTrigger(MEMENTO);
        return MementoEventProcessor.canUse(game, player)
                ? one(new MementoEvent(player.number()), new NoOpEvent(player.number()))
                : NONE;
    }
}
