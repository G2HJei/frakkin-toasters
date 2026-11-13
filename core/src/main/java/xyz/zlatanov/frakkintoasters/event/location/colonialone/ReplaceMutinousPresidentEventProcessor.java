package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor.canBecomePresident;

public class ReplaceMutinousPresidentEventProcessor extends EventProcessor<ReplaceMutinousPresidentEvent> {

    // todo the President may discard "Accept Prophecy" to keep his title, once Quorum Cards stay in play
    @Override
    public Followup process() {
        val president = game.president();
        if (president == null || game.player(game.playerNumber(president)).mutinyCards().isEmpty()) {
            return NONE;
        }
        // Asked only when another player can take the title, so the decision has an answer.
        val anyoneEligible = game.players().stream().anyMatch(p -> canBecomePresident(game, p.character()));
        return anyoneEligible ? single(new PlayerDecisionEvent<>(player.number(), MakePresidentEvent.class)) : NONE;
    }
}
