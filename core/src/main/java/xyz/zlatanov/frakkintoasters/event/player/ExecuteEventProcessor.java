package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.character.ForcedDiscards;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealCylonOnExecutionEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEventProcessor.canBeSelected;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class ExecuteEventProcessor extends EventProcessor<ExecuteEvent> {

    // A revealed Cylon may ignore an execution (rule 6.1.2.1), and a Cylon Leader counts as one unless Infiltrating.
    public static boolean canBeExecuted(GameView game, int playerNumber) {
        return game.player(playerNumber).isHuman();
    }

    @Override
    protected boolean isValid() {
        return canBeExecuted(game, event.playerNumber());
    }

    @Override
    public Followup process() {
        discardCards();
        val cylonCards = player.loyaltyCards().cards().stream().filter(LoyaltyCard::isCylon).distinct().toList();
        Followup followup;
        if (player.character().type() == CYLON_LEADER) {
            followup = cylonLeader();
        } else if (!cylonCards.isEmpty()) {
            // He reveals one "You Are a Cylon" card, his choice when he holds two different ones, and gives the others
            // away with his other Loyalty Cards.
            followup = one(cylonCards.stream()
                    .map(card -> new RevealCylonOnExecutionEvent(player.number(), card))
                    .toArray(Event[]::new));
        } else {
            followup = human();
        }
        if (player.number() == game.turn().currentPlayerNumber()) {
            // "If the executed character was being played by the current player, that player's turn ends, regardless
            // of whether his character was a human or Cylon."
            ops.endTurnEarly();
        }
        return followup;
    }

    // "He discards his hand of Skill and Mutiny Cards and his miracle token(s) (Quorum Cards in his hand are
    // unaffected)." The Quorum Cards given to his character go too, e.g. "Assign Mission Specialist". For an
    // Infiltrating Cylon Leader they go as the Infiltration ends (rule 6.2.3.5).
    private void discardCards() {
        val playerOps = ops.player(player.number());
        playerOps.discard(player.skillCards().cards().toArray(SkillCard[]::new));
        playerOps.discard(player.mutinyCards().cards().toArray(MutinyCard[]::new));
        playerOps.loseMiracleTokens();
        ops.titles().discardQuorumCardsGivenTo(player.number());
    }

    // "If a player is a Cylon Leader, he has no Loyalty Cards to reveal": his Infiltration ends at the "Resurrection
    // Ship". Cylon Leaders hold no titles (rule 6.2.3.3).
    private Followup cylonLeader() {
        ops.player(player.number()).endInfiltration();
        return NONE;
    }

    // "Lose 1 morale", the character leaves the game, and the player discards his Loyalty Cards and chooses a new
    // character. If he is the Mutineer, the new character keeps the card.
    private Followup human() {
        val playerNumber = player.number();
        val character = player.character();
        val loyaltyCards = player.loyaltyCards().cards().stream().filter(card -> card != MUTINEER).toList();
        val finalFive = loyaltyCards.stream().filter(LoyaltyCard::isFinalFive).toList();
        if (!event.withoutMoraleLoss() && ops.galactica().decreaseMorale(1) > 0) {
            ops.moraleLostToExecution(); // unless a "Preventative Policy" took the loss
        }
        ops.player(playerNumber).removeCharacter();
        // A Final Five card the execution reveals resolves, then leaves the game (rule 5.4.1.2).
        ops.player(playerNumber).removeLoyaltyCardsFromGame(finalFive);
        ops.player(playerNumber).discardLoyaltyCards(loyaltyCards.stream().filter(card -> !card.isFinalFive()).toList());
        // "He discards all his Loyalty Cards", the Personal Goals he revealed too.
        ops.player(playerNumber).discardLoyaltyCards(player.loyaltyCards().revealedCards().stream()
                .filter(card -> card.personalGoalResource() != null)
                .toList());
        if (Arrays.stream(Character.values()).noneMatch(c -> canBeSelected(game, c))) {
            // "If a human is executed and there are no available characters left to be selected, then the humans
            // immediately lose the game." (rule 7.2)
            return single(new CylonsWinEvent());
        }
        val followups = new ArrayList<Event>();
        finalFive.forEach(card -> followups.addAll(finalFiveRevealed(playerNumber)));
        followups.add(new PlayerDecisionEvent<>(playerNumber, SelectCharacterEvent.class));
        followups.add(new FinishExecutionEvent(playerNumber, character));
        return all(followups.toArray(Event[]::new));
    }

    // Each Final Five card revealed by an execution makes every player discard 2 Skill Cards, but the revealed Cylons
    // (rule 5.4.1.2). The executed player already discarded his hand.
    private List<Event> finalFiveRevealed(int executedPlayerNumber) {
        return playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .filter(other -> other.number() != executedPlayerNumber) // his character is gone
                .filter(PlayerView::isHuman)
                .filter(other -> !other.skillCards().isEmpty())
                .map(other -> ForcedDiscards.discard(game, other.number(), 2))
                .toList();
    }
}
