package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;

public class ConsultTheHybridPassEventProcessor extends EventProcessor<ConsultTheHybridPassEvent> {

    @Override
    public Followup process() {
        return single(new PlayerDecisionEvent<>(game.turn().currentPlayerNumber(), DrawSkillCardsEvent.class,
                DRAW_EXACTLY_2_OF_ANY_COLOR));
    }
}
