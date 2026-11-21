package xyz.zlatanov.frakkintoasters.event.character;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;

/**
 * The Skill Cards a negative ability makes a player discard to take an action. He pays first, then the action follows:
 * its processor records {@code ActionCostPaid} and queues the discard and the same event again. An action he can't pay
 * for isn't offered. A cost is a choice he makes, so "For Love" and "Headstrong" don't touch it.
 */
@NoArgsConstructor(access = PRIVATE)
public final class ActionCosts {

    // Laura Roslin's "Terminal Illness": 2 before she activates any location.
    static final int TERMINAL_ILLNESS_CARDS     = 2;
    // Caprica Six's "Conflicted Loyalties": 1 to activate the "Cylon Fleet".
    static final int CONFLICTED_LOYALTIES_CARDS = 1;
    // Louis Hoshi's "Reluctant": 1 to use a Skill Card's "Action:".
    static final int RELUCTANT_CARDS            = 1;

    /**
     * @return the Skill Cards the player discards before activating the location
     */
    public static int locationCost(PlayerView player, Location location) {
        int cost = player.hasAbility(TERMINAL_ILLNESS) ? TERMINAL_ILLNESS_CARDS : 0;
        if (location == CYLON_FLEET && player.hasAbility(CONFLICTED_LOYALTIES)) {
            cost += CONFLICTED_LOYALTIES_CARDS;
        }
        return cost;
    }

    /**
     * @return whether the player can pay for activating the location, or already did
     */
    public static boolean canPayLocationCost(GameView game, PlayerView player, Location location) {
        return game.turn().actionCostPaid(player.number())
                || player.skillCards().size() >= locationCost(player, location);
    }

    /**
     * @return the Skill Cards the player discards besides the card he plays for its "Action:"
     */
    public static int skillCardCost(PlayerView player, SkillCardType type) {
        return type.isAction() && player.hasAbility(RELUCTANT) ? RELUCTANT_CARDS : 0;
    }

    /**
     * @return whether the player can pay for playing a card of the type, or already did: other cards than the one he plays
     */
    public static boolean canPaySkillCardCost(GameView game, PlayerView player, SkillCardType type) {
        return game.turn().actionCostPaid(player.number())
                || player.skillCards().size() - 1 >= skillCardCost(player, type);
    }

    /**
     * @return "Reluctant"'s discard: a choice of the cards in his hand, but one copy of the card he plays, so he can't
     * discard the card he means to play
     */
    public static Followup skillCardDiscard(PlayerView player, SkillCard played) {
        val others = new ArrayList<>(player.skillCards().cards());
        others.remove(played);
        return one(others.stream()
                .distinct()
                .map(card -> (Event) new DiscardSkillCardsEvent(player.number(), List.of(card)))
                .toArray(Event[]::new));
    }
}
