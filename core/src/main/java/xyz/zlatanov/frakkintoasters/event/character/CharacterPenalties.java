package xyz.zlatanov.frakkintoasters.event.character;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.DiscardKind;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.CHOSEN;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.HAND;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.RANDOM;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;

@NoArgsConstructor(access = PRIVATE)
public final class CharacterPenalties {

    // Lee Adama's "Moral Dilemma": a Mutiny Card he draws costs him 2 Skill Cards.
    static final int MORAL_DILEMMA_CARDS = 2;

    /**
     * Cavil's "Lies and Secrets": whenever a player reveals a "You Are a Cylon" card, Cavil discards his hand.
     */
    public static Followup cylonCardRevealed(GameView game) {
        return all(game.players().stream()
                .filter(player -> player.hasAbility(LIES_AND_SECRETS) && !player.skillCards().isEmpty())
                .map(player -> ForcedDiscards.discard(game, player.number(), HAND, 0))
                .toArray(Event[]::new));
    }

    /**
     * Lee Adama's "Moral Dilemma", after the player got a Mutiny Card.
     */
    public static Followup mutinyCardDrawn(GameView game, PlayerView player) {
        return player.hasAbility(MORAL_DILEMMA) && !player.skillCards().isEmpty()
                ? all(ForcedDiscards.discard(game, player.number(), MORAL_DILEMMA_CARDS))
                : Followup.NONE;
    }

    /**
     * Saul Tigh's "Alcoholic": at the start of every turn, he discards his only Skill Card.
     */
    public static Followup turnStarted(GameView game) {
        return all(game.players().stream()
                .filter(player -> player.hasAbility(ALCOHOLIC) && player.skillCards().size() == 1)
                .map(player -> ForcedDiscards.discard(game, player.number(), HAND, 0))
                .toArray(Event[]::new));
    }

    /**
     * Brendan "Hot Dog" Costanza's "Forced to Eject": a viper he pilots is destroyed when it would be damaged.
     */
    public static boolean destroyedInsteadOfDamaged(GameView game, int humanFighterId) {
        return game.players().stream()
                .filter(player -> player.hasAbility(FORCED_TO_EJECT))
                .flatMap(player -> game.boards().galactica().pilotedFighter(player.character()).stream())
                .anyMatch(fighter -> fighter.id() == humanFighterId);
    }

    /**
     * Lee "Apollo" Adama's "Headstrong": the Skill Cards he is made to discard are random.
     */
    public static DiscardKind discardKind(GameView game, int playerNumber, DiscardKind kind) {
        return kind == CHOSEN && game.player(playerNumber).hasAbility(HEADSTRONG) ? RANDOM : kind;
    }
}
