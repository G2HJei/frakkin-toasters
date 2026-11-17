package xyz.zlatanov.frakkintoasters.projection;

import xyz.zlatanov.frakkintoasters.engine.PendingInput;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.card.SeenLoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;
import xyz.zlatanov.frakkintoasters.state.track.JumpPreparation;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The game as one viewer may see it, as plain records. What lies open on the table is public. What is hidden shows only
 * to who may know it, see {@link GameProjector}: a hidden card is a count (and the color of a Skill Card, which its back
 * shows), and a field that holds cards is {@code null} for a viewer who may not see them.
 *
 * @param input   what the game waits for, as this viewer may know it. {@code null} once the game has ended
 * @param outcome {@code null} while the game goes on
 */
public record GameProjection(
        Viewer viewer,
        ObjectiveCard objective,
        TurnProjection turn,
        List<PlayerProjection> players,
        Titles titles,
        GalacticaProjection galactica,
        PegasusProjection pegasus,
        CylonFleetProjection cylonFleet,
        CylonSupply cylonSupply,
        List<DeckProjection> decks,
        HiddenCards<QuorumCard> presidentHand,
        SkillCheckProjection skillCheck,
        int nukes,
        List<SkillCheckType> crisisCardsInPlay,
        List<DestinationCard> destinations,
        int distanceTraveled,
        List<Character> removedCharacters,
        List<ShipProjection> destroyedShips,
        InputProjection input,
        Outcome outcome) {

    public static GameProjection of(GameView game, Optional<PendingInput> pendingInput, Viewer viewer) {
        return new GameProjector(game, pendingInput, viewer).project();
    }

    /**
     * @param interruptWindow the open window, {@code null} for a viewer who may not know of it: it opens only when
     *                        someone holds a card for it
     * @param windowCards     the cards in the open window that the viewer played
     */
    public record TurnProjection(int number,
                                 GameStep step,
                                 int currentPlayer,
                                 int actingPlayer,
                                 List<CrisisCard> crisisCards,
                                 boolean endedEarly,
                                 boolean returnsToCrisisStep,
                                 DieRollProjection dieRoll,
                                 InterruptWindow interruptWindow,
                                 List<PlayedInterruptCard> windowCards,
                                 List<SkillCardType> resolvedInterruptCards,
                                 Map<Location, Integer> raidersToMiss) {
    }

    /**
     * @param value {@code null} until the die is rolled
     */
    public record DieRollProjection(Integer value, List<SkillCardType> cards, int modifier, Integer result) {
    }

    /**
     * @param location           {@code null} while the character is not on the board, or before one is chosen
     * @param human              {@code null} before a character is chosen
     * @param seenLoyaltyCards   the Loyalty Cards of others this player looked at, {@code null} for anyone else
     * @param knownCivilianShips the civilian ships this player looked at, {@code null} for anyone else
     */
    public record PlayerProjection(int number,
                                   Character character,
                                   Location location,
                                   SkillHand skillCards,
                                   HiddenCards<LoyaltyCard> loyaltyCards,
                                   HiddenCards<MotiveCard> motiveCards,
                                   HiddenCards<MutinyCard> mutinyCards,
                                   HiddenCards<SuperCrisisCard> superCrisisCards,
                                   boolean miracleToken,
                                   boolean infiltrating,
                                   Boolean human,
                                   boolean revealedCylon,
                                   boolean mutineer,
                                   int turns,
                                   int handLimit,
                                   List<SeenLoyaltyCard> seenLoyaltyCards,
                                   List<Integer> knownCivilianShips) {
    }

    /**
     * A hand of Skill Cards. Everyone sees how many of each color, because the back of a card shows it.
     *
     * @param cards {@code null} for a viewer who may not see them
     */
    public record SkillHand(int count, Map<SkillCardColor, Integer> colors, List<SkillCard> cards) {
    }

    /**
     * Cards that lie face down, and those that were revealed.
     *
     * @param count    the face down cards
     * @param revealed the cards everyone sees
     * @param cards    the face down cards, {@code null} for a viewer who may not see them
     */
    public record HiddenCards<T>(int count, List<T> revealed, List<T> cards) {
    }

    public record Titles(Character president, Character admiral, Character cag) {
    }

    public record GalacticaProjection(int fuel,
                                      int food,
                                      int morale,
                                      int population,
                                      JumpPreparation jumpPreparation,
                                      boolean colonialOneDestroyed,
                                      List<LocationProjection> locations,
                                      List<ShipProjection> ships,
                                      List<ShipProjection> reserves,
                                      List<ShipProjection> damagedVipers,
                                      List<CenturionProjection> boardingParty) {
    }

    public record PegasusProjection(boolean engineRoomActivated, List<LocationProjection> locations) {
    }

    public record CylonFleetProjection(Pursuit pursuit, List<LocationProjection> locations, List<ShipProjection> ships) {
    }

    public record LocationProjection(Location location, boolean damaged, List<Character> characters) {
    }

    /**
     * @param location {@code null} off the board, e.g. in the "Reserves"
     * @param pilot    the pilot of a viper or a raptor, otherwise {@code null}
     * @param damage   the damage of a basestar
     * @param cargo    what a civilian ship costs when it is destroyed, {@code null} for a viewer who didn't look
     */
    public record ShipProjection(int id, ShipType type, Location location, Character pilot, List<BasestarDamage> damage,
                                 Cargo cargo) {
    }

    public record Cargo(int fuel, int morale, int population) {
    }

    public record CenturionProjection(int id, BoardingParty position) {
    }

    public record CylonSupply(int basestars, int raiders, int heavyRaiders, int centurions) {
    }

    /**
     * @param discarded the discard pile, {@code null} when it lies face down
     * @param drawPile  the draw pile in order, {@code null} for every viewer of a game
     */
    public record DeckProjection(DeckId deck, int size, int discardSize, List<?> discarded, List<?> drawPile) {
    }

    /**
     * @param playedBefore the cards each player played before the check
     * @param cards        the face down pile of the cards played into the check, before they are shuffled and divided
     * @param difficulty   the difficulty with what reduces it so far
     */
    public record SkillCheckProjection(SkillCheckType type,
                                       Integer targetPlayer,
                                       int difficulty,
                                       List<PlayedBefore> playedBefore,
                                       boolean playedBeforeRevealed,
                                       SkillHand cards,
                                       List<SkillCard> matchingPile,
                                       List<SkillCard> nonMatchingPile,
                                       List<SkillCardType> resolvedAbilities,
                                       boolean emergencyDeclared,
                                       Integer totalStrength,
                                       SkillCheckResult result,
                                       boolean passResultReplaced,
                                       boolean failResultPrevented) {
    }

    public record PlayedBefore(int player, SkillHand cards) {
    }

    /**
     * What the game waits for, as one viewer may know it (backend decision 6).
     */
    public sealed interface InputProjection {
    }

    /**
     * The viewer has to answer with an event of the action's type.
     */
    public record Decide(int player, Class<? extends Event> action, List<EventConstraint> constraints)
            implements InputProjection {
    }

    /**
     * The viewer has to pick one of the options, by its index.
     */
    public record Choose(int player, List<Followup> options) implements InputProjection {
    }

    public record WaitingFor(int player) implements InputProjection {
    }

    /**
     * Someone answers a secret input: nobody else may know who.
     */
    public record Waiting() implements InputProjection {
    }

    public record Outcome(Team winner, List<Integer> winners) {
    }
}
