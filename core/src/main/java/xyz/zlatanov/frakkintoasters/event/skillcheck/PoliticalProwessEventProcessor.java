package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playOrPassDecisions;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.POLITICAL_PROWESS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;

public class PoliticalProwessEventProcessor extends EventProcessor<PoliticalProwessEvent> {

    @Override
    protected boolean isValid() {
        val card = event.politicalProwessCard();
        return card.type() == POLITICAL_PROWESS
                && player.skillCards().cards().contains(card)
                && game.activeSkillCheck().type().triggeredByLocation()
                && skillCheckNotMadeYet();
    }

    @Override
    public Followup process() {
        ops.player(player.number()).discard(event.politicalProwessCard());
        ops.skillCheck().determineResult(event.pass() ? PASS : FAIL);
        return event.pass()
                ? all(playOrPassDecisions(game, ChangeOfPlansEvent.class).toArray(Followup[]::new))
                : Followup.NONE;
    }

    private boolean skillCheckNotMadeYet() {
        val skillCheck = game.activeSkillCheck();
        return skillCheck.playedBeforeSkillCheck().isEmpty()
                && skillCheck.cards().isEmpty()
                && skillCheck.matchingPile().isEmpty()
                && skillCheck.nonMatchingPile().isEmpty()
                && skillCheck.result() == null;
    }
}
