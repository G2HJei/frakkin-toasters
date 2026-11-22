package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.POLITICAL_PROWESS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;

public class PoliticalProwessEventProcessor extends EventProcessor<PoliticalProwessEvent> {

    @Override
    protected boolean isValid() {
        val card = event.politicalProwessCard();
        return card != null
                && card.type() == POLITICAL_PROWESS
                && player.skillCards().cards().contains(card)
                && canPlay(game);
    }

    // Before a skill check triggered by a location is made.
    public static boolean canPlay(GameView game) {
        val skillCheck = game.activeSkillCheck();
        return skillCheck != null
                && skillCheck.type().triggeredByLocation()
                && skillCheck.playedBeforeSkillCheck().isEmpty()
                && skillCheck.cards().isEmpty()
                && skillCheck.matchingPile().isEmpty()
                && skillCheck.nonMatchingPile().isEmpty()
                && skillCheck.result() == null;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).discard(event.politicalProwessCard());
        // MakeSkillCheckEvent then skips the check and resolves the result.
        ops.skillCheck().determineResult(event.pass() ? PASS : FAIL);
        return Followup.NONE;
    }
}
