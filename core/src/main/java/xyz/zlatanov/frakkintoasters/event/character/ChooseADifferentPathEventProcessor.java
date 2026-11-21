package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.CrisisCardFollowups;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CHOOSE_A_DIFFERENT_PATH;

public class ChooseADifferentPathEventProcessor extends EventProcessor<ChooseADifferentPathEvent> {

    @Override
    protected boolean isValid() {
        val cards = game.turn().crisisCards();
        return CharacterAbilities.canUseMiracle(game, player, CHOOSE_A_DIFFERENT_PATH)
                && !cards.isEmpty()
                && cards.getLast() == event.card();
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(CHOOSE_A_DIFFERENT_PATH);
        return new CrisisCardFollowups(game).differentPath();
    }
}
