package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class TrustInstinctsEventProcessor extends EventProcessor<TrustInstinctsEvent> {

    @Override
    public Followup process() {
        return all(
                ops.skillCheck()
                        .playFromDestinyDeck(2, true)
                        .stream()
                        .filter(SkillCard::hasSkillCheckAbility)
                        .map(this::getFollowup)
                        .toArray(Followup[]::new));
    }


    private Followup getFollowup(SkillCard c) {
        return FollowupUtils.skillCheckAbilityFollowup(game.player(game.currentPlayer()), c);
    }
}
