package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.Title;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.TurnLimit;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.List;

public sealed interface GameFact extends Fact {

    record GameStarted(int playerCount, ObjectiveCard objective) implements GameFact {
    }

    record DieRollStarted() implements GameFact {
    }

    // A reroll is another DieRolled: it replaces the face and keeps the cards played on the roll.
    record DieRolled(int value) implements GameFact {
    }

    record DieRollCardPlayed(SkillCardType card, int modifier) implements GameFact {
    }

    // A character ability took effect on the roll, e.g. a reroll, which counts once per roll for "FTL Operator".
    record DieRollAbilityUsed(CharacterAbility ability) implements GameFact {
    }

    // An ability set the face instead of the die: "Hotshot", or "Longshot", whose result is fixed.
    record DieRollReplaced(int value, boolean fixed) implements GameFact {
    }

    record StepChanged(GameStep step) implements GameFact {
    }

    record TurnStarted(int turn) implements GameFact {
    }

    record TurnEndedEarly() implements GameFact {
    }

    record CurrentPlayerChanged(int playerNumber) implements GameFact {
    }

    record ActingPlayerChanged(Integer playerNumber) implements GameFact {
    }

    record CrisisCardPlayed(CrisisCard card) implements GameFact {
    }

    record CrisisCardKeptInPlay(SkillCheckType type) implements GameFact {
    }

    // "After the Activate Cylon Ships step, return to the Resolve Crisis step" ("Fulfiller of Prophecy").
    record ReturnToCrisisStepChanged(boolean returns) implements GameFact {
    }

    record NukeDiscarded() implements GameFact {
    }

    // An execution cost the fleet morale (rule 7.1), which "Eulogy" asks for.
    record MoraleLostToExecution() implements GameFact {
    }

    record NukeGained() implements GameFact {
    }

    // "Preventative Policy": the next loss of the resource this turn is 1 smaller.
    record LossReductionAdded(Resource resource) implements GameFact {
    }

    record LossReduced(Resource resource) implements GameFact {
    }

    // "Negotiation": no Cylon ship launches or activates for the rest of the turn.
    record CylonShipsHeld() implements GameFact {
    }

    record TurnLimitUsed(int playerNumber, TurnLimit limit) implements GameFact {
    }

    // The player may take the action of the location from wherever he is, e.g. Louis Hoshi's "Organized", until
    // RemoteActionEnded.
    record RemoteActionAllowed(int playerNumber, Location location) implements GameFact {
    }

    record RemoteActionEnded() implements GameFact {
    }

    // Where the current player was at the start of his turn, a location or a space area. Noted only for Louanne "Kat"
    // Katraine's "Stim Junkie".
    record StartLocationNoted(Location location) implements GameFact {
    }

    // The player goes to the "Brig" at the end of the turn, e.g. by Romo Lampkin's "Kleptomania".
    record SentToBrigAtEndOfTurn(int playerNumber) implements GameFact {
    }

    // The player paid the cost an ability puts on his action, e.g. Laura Roslin's "Terminal Illness", and the action
    // follows. null when it has followed.
    record ActionCostPaid(Integer playerNumber) implements GameFact {
    }

    // The current player takes the title for her turn (Ellen Tigh's "Manipulative"); it goes back at the end of it.
    record TitleBorrowed(Title title, Character previousHolder) implements GameFact {
    }

    // A Skill Card played from the hand for its ability, e.g. its "Action:". The turn keeps it for the cards limited per turn.
    record SkillCardPlayed(int playerNumber, SkillCard card) implements GameFact {
    }

    record CrisisCardsCleared(List<CrisisCard> cards) implements GameFact {
        public CrisisCardsCleared {
            cards = List.copyOf(cards);
        }
    }

    // The Destination Card goes next to the Objective Card, and its distance counts (rule 12.3).
    record DestinationReached(DestinationCard card) implements GameFact {
    }

    // @param winnerPlayerNumbers the numbers of the players who win, the Cylon Leaders that qualify included (rule 16.3.2)
    record GameEnded(Team winner, List<Integer> winnerPlayerNumbers) implements GameFact {
        public GameEnded {
            winnerPlayerNumbers = List.copyOf(winnerPlayerNumbers);
        }
    }
}
