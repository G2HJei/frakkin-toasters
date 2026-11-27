package xyz.zlatanov.frakkintoasters.event.character;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;

/**
 * The checks that offer an action ask them, so a decision always has an answer.
 */
@NoArgsConstructor(access = PRIVATE)
public final class CharacterBans {

    public static boolean canActivate(GameView game, PlayerView player, Location location) {
        // William Adama's "Emotionally Attached"
        if (location == ADMIRALS_QUARTERS && player.hasAbility(EMOTIONALLY_ATTACHED)) {
            return false;
        }
        // Helena Cain's "Bent on Revenge"
        if ((location == FTL_CONTROL || location == ENGINE_ROOM) && player.hasAbility(BENT_ON_REVENGE)) {
            return false;
        }
        // Tom Zarek's "Convicted Criminal": no location another character is in, but the "Brig".
        return location == BRIG || !player.hasAbility(CONVICTED_CRIMINAL) || !occupiedByAnother(game, player, location);
    }

    /**
     * @return whether the player may use the "Action:" of a Skill Card of the type: Sherman "Doc" Cottle's "Specialized"
     * forbids the Engineering ones. Human players can't use the text of Treachery Cards either (Ellen Tigh's sheet), but
     * no Treachery Card has an action: their text resolves in skill checks, for the current player (rule 10.4.2)
     */
    public static boolean canUseSkillCardAction(PlayerView player, SkillCardType type) {
        return !(type.isAction() && type.color() == ENGINEERING && player.hasAbility(SPECIALIZED));
    }

    /**
     * @return whether the player may take the action of a "You Are a Cylon" card: Gaius Baltar (alt.)'s "Spiritual
     * Crisis" allows it only in the "Brig"
     */
    public static boolean canUseCylonCardAction(GameView game, PlayerView player) {
        return !player.hasAbility(SPIRITUAL_CRISIS) || game.locate(player.character()).filter(BRIG::equals).isPresent();
    }

    private static boolean occupiedByAnother(GameView game, PlayerView player, Location location) {
        return game.players().stream()
                .filter(other -> other.number() != player.number() && other.character() != null)
                .anyMatch(other -> game.locate(other.character()).filter(location::equals).isPresent());
    }
}
