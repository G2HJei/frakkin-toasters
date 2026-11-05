package xyz.zlatanov.frakkintoasters.journal.fact;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
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

    record EmergencyDeclared() implements SkillCheckFact {
    }

    record TotalStrengthDetermined(int totalStrength) implements SkillCheckFact {
    }

    record ResultDetermined(SkillCheckResult result) implements SkillCheckFact {
    }

    record PassResultReplaced() implements SkillCheckFact {
    }

    record FailResultPrevented() implements SkillCheckFact {
    }
}
