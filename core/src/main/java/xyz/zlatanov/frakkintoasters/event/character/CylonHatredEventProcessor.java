package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CYLON_HATRED;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

public class CylonHatredEventProcessor extends EventProcessor<CylonHatredEvent> {

    static final int REDUCTION = 3;

    // As the location is activated, before the check is totaled.
    public static boolean canUse(GameView game, PlayerView player) {
        val skillCheck = game.activeSkillCheck();
        return player.hasAbility(CYLON_HATRED)
                && CharacterAbilities.canAffectSkillCheck(game)
                && skillCheck.type() == ADMIRALS_QUARTERS
                && skillCheck.result() == null
                && skillCheck.totalStrength() == null;
    }

    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(saul -> one(new CylonHatredEvent(saul.number()), new NoOpEvent(saul.number())))
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(CYLON_HATRED);
        ops.skillCheck().adjustDifficulty(-REDUCTION);
        return NONE;
    }
}
