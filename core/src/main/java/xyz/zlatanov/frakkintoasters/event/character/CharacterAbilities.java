package xyz.zlatanov.frakkintoasters.event.character;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.THE_FARM;

/**
 * Which character abilities a player can use now. Whether an ability is in effect at all is
 * {@link PlayerView#hasAbility}: the character prints it, and he isn't a revealed Cylon, whose abilities are ignored.
 */
@NoArgsConstructor(access = PRIVATE)
public final class CharacterAbilities {

    /**
     * @return whether the player can use the miracle ability now: he has it and the miracle tokens it spends, and no
     * "The Farm" keeps a human from it
     */
    public static boolean canUseMiracle(GameView game, PlayerView player, CharacterAbility ability) {
        if (!ability.isMiracle() || !player.hasAbility(ability) || player.miracleTokens() < ability.miracleTokens()) {
            return false;
        }
        // "The Farm" kept in play: "human players may not use their once-per-game abilities". A Cylon Leader who isn't
        // Infiltrating isn't human.
        return !(player.isHuman() && game.crisisCardsInPlay().contains(THE_FARM));
    }

    /**
     * @return whether the player can use an ability limited to once a turn: he has it and hasn't used it this turn
     */
    public static boolean canUseOncePerTurn(GameView game, PlayerView player, CharacterAbility ability) {
        return player.hasAbility(ability) && !game.turn().hasUsed(player.number(), ability);
    }

    /**
     * @return whether character abilities may affect the skill check under way: a Super Crisis Card's is immune to the
     * ones that affect Crisis Cards or skill checks (rule 6.1.2.5)
     */
    public static boolean canAffectSkillCheck(GameView game) {
        return game.activeSkillCheck() != null && !game.activeSkillCheck().type().isSuperCrisis();
    }
}
