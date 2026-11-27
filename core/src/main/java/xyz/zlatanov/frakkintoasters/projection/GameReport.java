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
 * Read from the journal once the game has ended, when nothing is hidden any more, so it is the same for every viewer.
 */
public record GameReport(Team winner,
                         List<Integer> winners,
                         int turns,
                         int distance,
                         List<PlayerReport> players,
                         GameStatistics statistics,
                         List<TurnResources> resources,
                         List<Moment> story) {

    public static Optional<GameReport> of(List<JournalEntry> journal) {
        return new GameReporter(journal).report();
    }

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

    public record PlayerStatistics(int skillCardsGained,
                                   int cardsIntoChecks,
                                   int strengthIntoChecks,
                                   int skillCardsPlayed,
                                   int crisisCards,
                                   int abilitiesUsed,
                                   int sentToBrig) {
    }

    public record GameStatistics(int crisisCards,
                                 Map<SkillCheckResult, Integer> skillChecks,
                                 int jumps,
                                 Map<ShipType, Integer> shipsLost,
                                 int vipersDamaged,
                                 int locationsDamaged,
                                 int nukesUsed) {
    }

    public record TurnResources(int turn, boolean end, int fuel, int food, int morale, int population, int distance) {
    }

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
