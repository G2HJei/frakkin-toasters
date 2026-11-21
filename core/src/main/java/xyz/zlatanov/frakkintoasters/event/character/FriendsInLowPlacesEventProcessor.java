package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FRIENDS_IN_LOW_PLACES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.BRIG;

public class FriendsInLowPlacesEventProcessor extends EventProcessor<FriendsInLowPlacesEvent> {

    static final int ADJUSTMENT = 2;

    public static boolean canUse(GameView game, PlayerView player) {
        val skillCheck = game.activeSkillCheck();
        return player.hasAbility(FRIENDS_IN_LOW_PLACES)
                && CharacterAbilities.canAffectSkillCheck(game)
                && skillCheck.type() == BRIG
                && skillCheck.result() == null
                && skillCheck.totalStrength() == null;
    }

    /**
     * @return the choice to raise, lower or keep the difficulty, as the "Brig" is activated
     */
    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(zarek -> one(
                        new FriendsInLowPlacesEvent(zarek.number(), -ADJUSTMENT),
                        new FriendsInLowPlacesEvent(zarek.number(), ADJUSTMENT),
                        new NoOpEvent(zarek.number())))
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return Math.abs(event.adjustment()) == ADJUSTMENT && canUse(game, player);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(FRIENDS_IN_LOW_PLACES);
        ops.skillCheck().adjustDifficulty(event.adjustment());
        return NONE;
    }
}
