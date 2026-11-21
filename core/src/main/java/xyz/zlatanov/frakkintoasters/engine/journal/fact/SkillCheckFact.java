package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckPile;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.List;

/**
 * Facts about the active skill check.
 */
public sealed interface SkillCheckFact extends Fact {

    record SkillCheckStarted(SkillCheckType type) implements SkillCheckFact {
    }

    record TargetChosen(int playerNumber) implements SkillCheckFact {
    }

    record CardsPlayedBefore(int playerNumber, List<SkillCard> cards) implements SkillCheckFact {
        public CardsPlayedBefore {
            cards = List.copyOf(cards);
        }
    }

    record PlayedBeforeRevealed() implements SkillCheckFact {
    }

    // "Investigative Committee": the cards a player put into the check, face up for everyone.
    record CardsShown(int playerNumber, List<SkillCard> cards) implements SkillCheckFact {
        public CardsShown {
            cards = List.copyOf(cards);
        }
    }

    record CardsPlaced(SkillCheckPile pile, List<SkillCard> cards, boolean onTop) implements SkillCheckFact {
        public CardsPlaced {
            cards = List.copyOf(cards);
        }
    }

    record CardsDrawn(SkillCheckPile pile, List<SkillCard> cards) implements SkillCheckFact {
        public CardsDrawn {
            cards = List.copyOf(cards);
        }
    }

    record CardRemoved(SkillCard card, Integer playerNumber) implements SkillCheckFact {
    }

    record AbilityResolved(SkillCardType type) implements SkillCheckFact {
    }

    record EmergencyDeclared() implements SkillCheckFact {
    }

    // A Quorum Card in play changed the difficulty, e.g. "Assign Arbitrator".
    record DifficultyAdjusted(int delta) implements SkillCheckFact {
    }

    // "Assign Chief of Staff": the Politics Cards in the check count positive.
    record PoliticsCountPositive() implements SkillCheckFact {
    }

    // The player looked at cards another player put into the check, e.g. through "Probation". It changes nothing.
    record CardsLookedAt(int playerNumber, int targetPlayerNumber, List<SkillCard> cards) implements SkillCheckFact {
        public CardsLookedAt {
            cards = List.copyOf(cards);
        }
    }

    record TotalStrengthDetermined(int totalStrength) implements SkillCheckFact {
    }

    record ResultDetermined(SkillCheckResult result) implements SkillCheckFact {
    }

    record PassResultReplaced() implements SkillCheckFact {
    }

    record FailResultPrevented() implements SkillCheckFact {
    }

    record SkillCheckEnded() implements SkillCheckFact {
    }
}
