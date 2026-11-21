package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MYSTERIOUS_INTUITION;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;

public class MysteriousIntuitionEventProcessor extends EventProcessor<MysteriousIntuitionEvent> {

    public static boolean canUse(GameView game, PlayerView player) {
        val skillCheck = game.activeSkillCheck();
        return CharacterAbilities.canUseMiracle(game, player, MYSTERIOUS_INTUITION)
                && skillCheck != null
                && skillCheck.type().onCrisisCard()
                && skillCheck.result() == null
                && skillCheck.playedBeforeSkillCheck().isEmpty()
                && skillCheck.cards().isEmpty();
    }

    /**
     * @return the choice of a pass, a fail or neither, before a Crisis Card's skill check is made, for the player who can
     * use the ability
     */
    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(boomer -> one(
                        new MysteriousIntuitionEvent(boomer.number(), true),
                        new MysteriousIntuitionEvent(boomer.number(), false),
                        new NoOpEvent(boomer.number())))
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(MYSTERIOUS_INTUITION);
        // MakeSkillCheckEvent then skips the check and resolves the result, as after "Political Prowess".
        ops.skillCheck().determineResult(event.pass() ? PASS : FAIL);
        return NONE;
    }
}
