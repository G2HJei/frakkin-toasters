package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.HUMAN_DELUSION;

public class HumanDelusionEventProcessor extends EventProcessor<HumanDelusionEvent> {

    public static boolean canUse(GameView game, PlayerView player) {
        val skillCheck = game.activeSkillCheck();
        return CharacterAbilities.canUseMiracle(game, player, HUMAN_DELUSION)
                && CharacterAbilities.canAffectSkillCheck(game)
                && skillCheck.totalStrength() == null
                && skillCheck.result() == null
                && !player.skillCards().cards().isEmpty();
    }

    /**
     * @return the choice of cards to add or nothing, once the cards of the check are revealed, for the player who can
     * use "Human Delusion"
     */
    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(six -> one(new PlayerDecisionEvent<>(six.number(), HumanDelusionEvent.class),
                        new NoOpEvent(six.number())))
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player)
                && event.cards() != null
                && !event.cards().isEmpty()
                && holdsSkillCards(event.cards());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(HUMAN_DELUSION);
        // They come after the reveal: they count for the strength, but their abilities aren't resolved.
        ops.skillCheck().addRevealedCards(player.number(), event.cards());
        return NONE;
    }
}
