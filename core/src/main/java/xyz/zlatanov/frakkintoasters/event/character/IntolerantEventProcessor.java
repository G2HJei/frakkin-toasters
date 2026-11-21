package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.INTOLERANT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

public class IntolerantEventProcessor extends EventProcessor<IntolerantEvent> {

    static final int STRENGTH = 10;

    /**
     * @return the player who may execute the target of the "Admiral's Quarters" check that just passed instead
     */
    public static Optional<PlayerView> user(GameView game) {
        val skillCheck = game.activeSkillCheck();
        // A pass the check reached, not one "Political Prowess" decided: it has a strength.
        if (!CharacterAbilities.canAffectSkillCheck(game)
                || skillCheck.type() != ADMIRALS_QUARTERS
                || skillCheck.result() != PASS
                || skillCheck.totalStrength() == null
                || skillCheck.totalStrength() < STRENGTH
                || !ExecuteEventProcessor.canBeExecuted(game, skillCheck.targetPlayerNumber())) {
            return Optional.empty();
        }
        return game.players().stream().filter(player -> player.hasAbility(INTOLERANT)).findFirst().map(PlayerView.class::cast);
    }

    @Override
    protected boolean isValid() {
        return user(game).filter(cain -> cain.number() == player.number()).isPresent()
                && event.targetPlayerNumber() == game.activeSkillCheck().targetPlayerNumber();
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(INTOLERANT);
        return single(new ExecuteEvent(event.targetPlayerNumber()));
    }
}
