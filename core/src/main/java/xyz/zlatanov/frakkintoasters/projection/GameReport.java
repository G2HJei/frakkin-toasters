package xyz.zlatanov.frakkintoasters.projection;

import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A finished game told whole, for its results: who won, who everyone was, the story and the statistics. It is read
 * from the journal once the game has ended, when nothing is hidden any more, so it is the same for every viewer. See
 * {@link GameReporter}.
 *
 * @param turns     the number of the last turn
 * @param resources the resources at the start of every turn, and at the end
 * @param story     what changed the course of the game, in order
 */
public record GameReport(Team winner,
                         List<Integer> winners,
                         int turns,
                         int distance,
                         List<PlayerReport> players,
                         GameStatistics statistics,
                         List<TurnResources> resources,
                         List<Moment> story) {

    /**
     * @return the report of the game, empty while it goes on
     */
    public static Optional<GameReport> of(List<JournalEntry> journal) {
        return new GameReporter(journal).report();
    }

    /**
     * @param character        the character the player ended with
     * @param formerCharacters the characters he had before, executed or removed
     * @param loyaltyCards     all his Loyalty Cards, revealed or not
     * @param cylon            he ended as a Cylon: a Cylon Loyalty Card, or a Cylon Leader
     * @param revealedOnTurn   the turn he revealed himself as a Cylon, {@code null} if he didn't
     */
    public record PlayerReport(int number,
                               Character character,
                               List<Character> formerCharacters,
                               List<LoyaltyCard> loyaltyCards,
                               List<MotiveCard> motiveCards,
                               boolean cylon,
                               Integer revealedOnTurn,
                               int turns,
                               PlayerStatistics statistics) {
    }

    /**
     * @param skillCardsGained   the Skill Cards that came into his hand
     * @param cardsIntoChecks    the Skill Cards he played into skill checks
     * @param strengthIntoChecks the strength of those cards, as printed
     * @param skillCardsPlayed   the Skill Cards he played for their text: "Action:", interrupts and the like
     * @param crisisCards        the Crisis Cards drawn on his turns
     * @param abilitiesUsed      the times he used one of his character's abilities
     * @param sentToBrig         the times his character was sent to the "Brig"
     */
    public record PlayerStatistics(int skillCardsGained,
                                   int cardsIntoChecks,
                                   int strengthIntoChecks,
                                   int skillCardsPlayed,
                                   int crisisCards,
                                   int abilitiesUsed,
                                   int sentToBrig) {
    }

    /**
     * @param skillChecks       the skill checks by their result
     * @param shipsLost         the ships destroyed or removed from the game, by type
     * @param vipersDamaged     the times a viper was damaged
     * @param locationsDamaged  the times a location of Galactica or Pegasus was damaged
     */
    public record GameStatistics(int crisisCards,
                                 Map<SkillCheckResult, Integer> skillChecks,
                                 int jumps,
                                 Map<ShipType, Integer> shipsLost,
                                 int vipersDamaged,
                                 int locationsDamaged,
                                 int nukesUsed) {
    }

    /**
     * @param turn the turn it starts, or the last one at the end
     * @param end  the resources at the end of the game
     */
    public record TurnResources(int turn, boolean end, int fuel, int food, int morale, int population, int distance) {
    }

    /**
     * A turning point of the game.
     *
     * @param playerNumber the player it is about, if any
     * @param character    the character it is about: the one revealed, removed, or given the title
     * @param destination  the destination of a jump
     * @param distance     the distance traveled after a jump
     * @param winner       the team that won, at the end
     */
    public record Moment(int turn,
                         MomentKind kind,
                         Integer playerNumber,
                         Character character,
                         DestinationCard destination,
                         Integer distance,
                         Team winner) {
    }

    public enum MomentKind {
        JUMPED,
        CYLON_REVEALED,
        CHARACTER_REMOVED,
        PRESIDENT,
        ADMIRAL,
        CAG,
        PEGASUS_DESTROYED,
        GAME_ENDED
    }
}
