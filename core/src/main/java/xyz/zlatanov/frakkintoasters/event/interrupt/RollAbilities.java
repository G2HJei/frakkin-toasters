package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterFtlControlRoll;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.DieRollView;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.character.CharacterAbilities.canUseMiracle;
import static xyz.zlatanov.frakkintoasters.event.character.CharacterAbilities.canUseOncePerTurn;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTION;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;

/**
 * The character abilities used in an interrupt window on its die roll. "Longshot" and "Hotshot" take the place of the
 * roll, so they are used in a window before it; "ECO Officer" and "FTL Operator" reroll, so in a window after it.
 */
@NoArgsConstructor(access = PRIVATE)
public final class RollAbilities {

    public static final List<CharacterAbility> ABILITIES = List.of(ECO_OFFICER, FTL_OPERATOR, LONGSHOT, HOTSHOT);

    private static final int HOTSHOT_BONUS = 2;

    public static boolean hasAbilityFor(GameView game, PlayerView player, InterruptWindow window) {
        return ABILITIES.stream().anyMatch(ability -> canUse(game, player, ability, window));
    }

    public static boolean canUse(GameView game, PlayerView player, CharacterAbility ability, InterruptWindow window) {
        val roll = game.turn().dieRoll();
        if (roll == null || ability == null) {
            return false;
        }
        val onHisTurn = player.number() == game.turn().currentPlayerNumber();
        return switch (ability) {
            // Helo, during his turn, once a turn: reroll the die just rolled.
            case ECO_OFFICER -> window.followsDieRoll() && canReroll(roll) && onHisTurn
                    && canUseOncePerTurn(game, player, ECO_OFFICER);
            // Gaeta, whenever "FTL Control" is activated: reroll its die, once for each activation.
            case FTL_OPERATOR -> window instanceof AfterFtlControlRoll && canReroll(roll)
                    && player.hasAbility(FTL_OPERATOR) && !roll.abilities().contains(FTL_OPERATOR);
            // Anders, once a game, on his turn: choose a roll's result instead of rolling.
            case LONGSHOT -> window.startsDieRoll() && onHisTurn && canUseMiracle(game, player, LONGSHOT);
            // Kat, when she would roll in her own Action step: a discarded card's strength +2 instead.
            case HOTSHOT -> window.startsDieRoll() && onHisTurn
                    && player.number() == game.turn().actingPlayerNumber()
                    && game.turn().step() == ACTION
                    && player.hasAbility(HOTSHOT)
                    && !player.skillCards().cards().isEmpty();
            default -> false;
        };
    }

    // A face an ability set ("Hotshot", "Longshot") has no die to reroll.
    public static boolean canReroll(DieRollView roll) {
        return roll.value() != null && !roll.replaced();
    }

    public static boolean rerolls(CharacterAbility ability) {
        return ability == ECO_OFFICER || ability == FTL_OPERATOR;
    }

    public static int hotshotResult(SkillCard card) {
        return card.value() + HOTSHOT_BONUS;
    }
}
