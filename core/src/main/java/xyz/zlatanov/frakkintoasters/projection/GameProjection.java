package xyz.zlatanov.frakkintoasters.projection;

import xyz.zlatanov.frakkintoasters.engine.PendingInput;
import xyz.zlatanov.frakkintoasters.engine.answer.LegalAnswers;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCardInPlay;
import xyz.zlatanov.frakkintoasters.state.card.SeenLoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.UsedRollAbility;
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
 * What is hidden shows only to who may know it, see {@link GameProjector}: a hidden card is a count, its back doesn't
 * show even the color of a Skill Card, and a field that holds cards is {@code null} for a viewer who may not see them.
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
        List<QuorumCardInPlay> quorumCardsInPlay,
        SkillCheckProjection skillCheck,
        int nukes,
        List<SkillCheckType> crisisCardsInPlay,
        List<DestinationCard> destinations,
        int distanceTraveled,
        List<Character> removedCharacters,
        List<ShipProjection> destroyedShips,
        InputProjection input,
        List<AnytimeAnswers> anytime,
        Outcome outcome) {

    public static GameProjection of(GameView game, Optional<PendingInput> pendingInput, Viewer viewer) {
        return of(game, pendingInput, Optional.empty(), viewer);
    }

    public static GameProjection of(GameView game, Optional<PendingInput> pendingInput, Optional<ScopeOpened> asking,
                                    Viewer viewer) {
        return new GameProjector(game, pendingInput, asking, viewer).project();
    }

    /**
     * @param interruptWindow {@code null} for a viewer who may not know of it: it opens only when someone holds a card
     *                        for it
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
                                 List<UsedRollAbility> windowAbilities,
                                 List<SkillCardType> resolvedInterruptCards,
                                 Map<Location, Integer> raidersToMiss,
                                 Map<Resource, Integer> lossReductions) {
    }

    /**
     * @param fixed "Longshot" chose the result, which nothing modifies
     */
    public record DieRollProjection(Integer value, List<SkillCardType> cards, int modifier, Integer result,
                                    List<CharacterAbility> abilities, boolean fixed) {
    }

    public record PlayerProjection(int number,
                                   Character character,
                                   Location location,
                                   SkillHand skillCards,
                                   HiddenCards<LoyaltyCard> loyaltyCards,
                                   HiddenCards<MotiveCard> motiveCards,
                                   HiddenCards<MutinyCard> mutinyCards,
                                   HiddenCards<SuperCrisisCard> superCrisisCards,
                                   HiddenCards<QuorumCard> quorumCards,
                                   int miracleTokens,
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
     * The others see only how many there are.
     */
    public record SkillHand(int count, List<SkillCard> cards) {
    }

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

    public record PegasusProjection(boolean engineRoomActivated, boolean destroyed, List<LocationProjection> locations) {
    }

    public record CylonFleetProjection(Pursuit pursuit, List<LocationProjection> locations, List<ShipProjection> ships) {
    }

    public record LocationProjection(Location location, boolean damaged, List<Character> characters) {
    }

    /**
     * @param cargo {@code null} for a viewer who didn't look
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

    public record DeckProjection(DeckId deck, int size, int discardSize, List<?> discarded, List<?> drawPile) {
    }

    public record SkillCheckProjection(SkillCheckType type,
                                       Integer targetPlayer,
                                       int difficulty,
                                       List<PlayedBefore> playedBefore,
                                       boolean playedBeforeRevealed,
                                       List<ShownCards> shownCards,
                                       SkillHand cards,
                                       List<SkillCard> matchingPile,
                                       List<SkillCard> nonMatchingPile,
                                       List<SkillCardType> resolvedAbilities,
                                       boolean emergencyDeclared,
                                       boolean politicsCountPositive,
                                       SkillCardColor zeroStrengthColor,
                                       Integer totalStrength,
                                       SkillCheckResult result,
                                       boolean passResultReplaced,
                                       boolean failResultPrevented) {
    }

    public record PlayedBefore(int player, SkillHand cards) {
    }

    // "Investigative Committee": the cards a player put into the check, face up for everyone.
    public record ShownCards(int player, List<SkillCard> cards) {
    }

    /**
     * What the game waits for, as one viewer may know it (backend decision 6).
     */
    public sealed interface InputProjection {
    }

    public record Decide(int player, Class<? extends Event> action, List<EventConstraint> constraints,
                         LegalAnswers answers, Topic topic) implements InputProjection {
    }

    /**
     * @param answers so the table can show what the option leads to before it is picked
     */
    public record Choose(int player, List<Followup> options, List<OptionAnswers> answers, Topic topic)
            implements InputProjection {
    }

    public record OptionAnswers(int option, LegalAnswers answers) {
    }

    public record WaitingFor(int player, Topic topic) implements InputProjection {
    }

    public record AnytimeAnswers(int player, LegalAnswers answers) {
    }

    /**
     * What an input is about: the event that queued it, e.g. the Crisis Card whose section is chosen, as the log shows its
     * scope. {@code null} on the input when no event queued it, or when the log hides that event's scope from the
     * viewer (a {@code SecretEvent} of another player).
     *
     * @param details the event, {@code null} when its cards aren't public ({@code Disclosure.isPublic})
     */
    public record Topic(Class<? extends Event> event, Event details) {
    }

    /**
     * Someone answers a secret input: nobody else may know who.
     */
    public record Waiting() implements InputProjection {
    }

    public record Outcome(Team winner, List<Integer> winners) {
    }
}
