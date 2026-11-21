package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor.canBecomePresident;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ACCEPT_PROPHECY;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_VICE_PRESIDENT;

public class ReplaceMutinousPresidentEventProcessor extends EventProcessor<ReplaceMutinousPresidentEvent> {

    @Override
    public Followup process() {
        val president = game.president();
        if (president == null || game.player(game.playerNumber(president)).mutinyCards().isEmpty()) {
            return NONE;
        }
        if (game.inPlay(ACCEPT_PROPHECY)) {
            // The President may first discard "Accept Prophecy" to keep the title. Both options name him.
            val presidentNumber = game.playerNumber(president);
            return one(new DefendPresidencyEvent(presidentNumber, true, player.number()),
                    new DefendPresidencyEvent(presidentNumber, false, player.number()));
        }
        return replacement(game, player.number());
    }

    /**
     * @return the new President: the Vice President's character while he isn't President, otherwise the choice of
     * the player of the "Administration". Nobody when no one can take the title, so the decision has an answer
     */
    static Followup replacement(GameView game, int administrationPlayerNumber) {
        val vicePresident = game.quorumCardHolder(ASSIGN_VICE_PRESIDENT)
                .map(holder -> game.player(holder).character())
                .filter(character -> character != game.president());
        if (vicePresident.isPresent()) {
            // nobody else may be chosen, so there is nothing to choose
            return canBecomePresident(game, vicePresident.get()) ? single(new MakePresidentEvent(vicePresident.get())) : NONE;
        }
        val anyoneEligible = game.players().stream().anyMatch(p -> canBecomePresident(game, p.character()));
        return anyoneEligible
                ? single(new PlayerDecisionEvent<>(administrationPlayerNumber, MakePresidentEvent.class))
                : NONE;
    }
}
