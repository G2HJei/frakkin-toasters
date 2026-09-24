package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.ABetterMachineEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.DogfightEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.ForceTheirHandEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.QuickThinkingEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DetermineSkillCheckAbilitiesOrderEventProcessor extends EventProcessor<DetermineSkillCheckAbilitiesOrderEvent> {
    @Override
    public Followup process() {
        val followups = event.cardsWithAbilities()
                .stream()
                .map(this::buildFollowup)
                .filter(f -> !f.equals(Followup.NONE))
                .toList();
        return followups.isEmpty()
                ? Followup.NONE
                : Followup.all(followups.toArray(Followup[]::new));
    }

    private Followup buildFollowup(SkillCard skillCard) {
        return switch (skillCard.type()) {
            case DOGFIGHT -> single(new PlayerDecisionEvent<>(player.number(), DogfightEvent.class));
            case FORCE_THEIR_HAND -> player.isHuman()
                    ? single(new PlayerDecisionEvent<>(player.number(), ForceTheirHandEvent.class))
                    : Followup.NONE;
            case QUICK_THINKING -> single(new PlayerDecisionEvent<>(player.number(), QuickThinkingEvent.class));
            case A_BETTER_MACHINE -> single(new ABetterMachineEvent());
            default -> Followup.NONE;
        };
    }
}
