package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playCardsBeforeSkillCheck;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playOrPassDecisions;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;

public class MakeSkillCheckEventProcessor extends EventProcessor<MakeSkillCheckEvent> {

    @Override
    protected boolean isValid() {
        return game.activeSkillCheck() != null;
    }

    @Override
    public Followup process() {
        val result = game.activeSkillCheck().result();
        if (result == null) {
            return playCardsBeforeSkillCheck(game);
        }
        // "Political Prowess": "Do not make the skill check; instead, it automatically passes or fails". A pass can
        // still be replaced by "Change of Plans".
        val followups = new ArrayList<Followup>();
        if (result == PASS) {
            followups.addAll(playOrPassDecisions(game, ChangeOfPlansEvent.class));
        }
        followups.add(single(new ResolveSkillCheckResultEvent()));
        followups.add(single(new EndSkillCheckEvent()));
        return all(followups.toArray(Followup[]::new));
    }
}
