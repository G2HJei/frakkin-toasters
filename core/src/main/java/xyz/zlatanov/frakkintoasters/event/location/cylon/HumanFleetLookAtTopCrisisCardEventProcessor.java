package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;

public class HumanFleetLookAtTopCrisisCardEventProcessor extends EventProcessor<HumanFleetLookAtTopCrisisCardEvent> {

    @Override
    public Followup process() {
        val playerNumber = event.playerNumber();
        val card = ops.player(playerNumber).drawCrisisCard();
        return all(
                Followup.one(
                        new PlaceCrisisCardOnTopEvent(playerNumber, card),
                        new PlaceCrisisCardOnBottomEvent(playerNumber, card)),
                single(
                        new PlayerDecisionEvent<>(playerNumber, DrawSkillCardsEvent.class, DRAW_EXACTLY_2))
        );
    }
}
