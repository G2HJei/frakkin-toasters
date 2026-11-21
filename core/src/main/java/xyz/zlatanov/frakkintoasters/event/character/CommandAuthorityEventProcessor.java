package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.COMMAND_AUTHORITY;

public class CommandAuthorityEventProcessor extends EventProcessor<CommandAuthorityEvent> {

    // The cards used in the check are the revealed ones; the check has a result by then.
    public static boolean canUse(GameView game, PlayerView player) {
        val skillCheck = game.activeSkillCheck();
        return CharacterAbilities.canUseMiracle(game, player, COMMAND_AUTHORITY)
                && CharacterAbilities.canAffectSkillCheck(game)
                && skillCheck.result() != null
                && !skillCheck.dividedCards().isEmpty();
    }

    /**
     * @return the choice to take the cards or not, for the player who can use "Command Authority" now
     */
    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> canUse(game, player))
                .findFirst()
                .map(adama -> one(new CommandAuthorityEvent(adama.number()), new NoOpEvent(adama.number())))
                .orElse(NONE);
    }

    /**
     * @return whether a player has "Command Authority" with the tokens to use it, so the end of a skill check asks
     * whether he can use it ({@link OfferCommandAuthorityEvent}). Games without him are left alone
     */
    public static boolean mayBeOffered(GameView game) {
        return game.players().stream()
                .anyMatch(player -> CharacterAbilities.canUseMiracle(game, player, COMMAND_AUTHORITY));
    }

    @Override
    protected boolean isValid() {
        return canUse(game, player);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(COMMAND_AUTHORITY);
        // The end of the check then discards only what is left: the cards played before it.
        game.activeSkillCheck().dividedCards().forEach(card -> ops.skillCheck().takeRevealedCard(card, player.number()));
        return NONE;
    }
}
