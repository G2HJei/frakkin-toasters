package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.GiveLoyaltyCardsEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.PassMutineerCardEvent;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;
import java.util.function.IntFunction;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEventProcessor.canBeSelected;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class ExecuteEventProcessor extends EventProcessor<ExecuteEvent> {

    // A revealed Cylon may ignore an execution (rule 6.1.2.1), and a Cylon Leader counts as one unless Infiltrating.
    public static boolean canBeExecuted(GameView game, int playerNumber) {
        return game.player(playerNumber).isHuman();
    }

    @Override
    protected boolean isValid() {
        return isPlayer(event.playerNumber()) && canBeExecuted(game, event.playerNumber());
    }

    @Override
    public Followup process() {
        discardCards();
        val cylonCard = player.loyaltyCards().cards().stream().filter(LoyaltyCard::isCylon).findFirst();
        Followup followup;
        if (player.character().type() == CYLON_LEADER) {
            followup = cylonLeader();
        } else if (cylonCard.isPresent()) {
            followup = cylon(cylonCard.get());
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
    // unaffected)."
    private void discardCards() {
        val playerOps = ops.player(player.number());
        val skillCards = player.skillCards().cards();
        if (!skillCards.isEmpty()) {
            playerOps.discard(skillCards.toArray(SkillCard[]::new));
        }
        val mutinyCards = player.mutinyCards().cards();
        if (!mutinyCards.isEmpty()) {
            playerOps.discard(mutinyCards.toArray(MutinyCard[]::new));
        }
        playerOps.loseMiracleToken();
    }

    // "If a player is a Cylon Leader, he has no Loyalty Cards to reveal": he returns to the "Resurrection Ship". Cylon
    // Leaders hold no titles (rule 6.2.3.3).
    private Followup cylonLeader() {
        ops.player(player.number()).endInfiltration();
        ops.player(player.number()).sendTo(RESURRECTION_SHIP);
        return NONE;
    }

    // He reveals a "You Are a Cylon" card without taking its action, hands his other Loyalty Cards and the Mutineer
    // card to humans of his choice, and resurrects like a revealed Cylon, without a Super Crisis Card.
    //todo verify which "You Are a Cylon" card is revealed in case player has more than one
    private Followup cylon(LoyaltyCard cylonCard) {
        val playerNumber = player.number();
        ops.player(playerNumber).revealLoyaltyCard(cylonCard);
        val remainingCards = player.loyaltyCards().cards().stream().anyMatch(card -> card != MUTINEER);
        val giveCards = remainingCards ? toAHuman(human -> new GiveLoyaltyCardsEvent(playerNumber, human)) : NONE;
        val passMutineer = player.isMutineer() ? toAHuman(human -> new PassMutineerCardEvent(playerNumber, human)) : NONE;
        ops.player(playerNumber).sendTo(RESURRECTION_SHIP);
        return allOf(giveCards, passMutineer, single(new GiveTitlesToNextInLineEvent(playerNumber)));
    }

    // "Lose 1 morale", the character leaves the game, and the player discards his Loyalty Cards and chooses a new
    // character. If he is the Mutineer, the new character keeps the card.
    // todo resolve a Final Five card revealed by the execution (rule 5.4.1.2)
    private Followup human() {
        val playerNumber = player.number();
        val character = player.character();
        ops.galactica().decreaseMorale(1);
        ops.player(playerNumber).removeCharacter();
        ops.player(playerNumber).discardLoyaltyCards(player.loyaltyCards().cards().stream()
                .filter(card -> card != MUTINEER)
                .toList());
        if (Arrays.stream(Character.values()).noneMatch(c -> canBeSelected(game, c))) {
            // "If a human is executed and there are no available characters left to be selected, then the humans
            // immediately lose the game." (rule 7.2)
            return single(new CylonsWinEvent());
        }
        return all(
                new PlayerDecisionEvent<>(playerNumber, SelectCharacterEvent.class),
                new FinishExecutionEvent(playerNumber, character));
    }

    private Followup toAHuman(IntFunction<Event> event) {
        return one(playersFrom(game, player.number()).stream()
                .filter(PlayerView::isHuman)
                .map(human -> event.apply(human.number()))
                .toArray(Event[]::new));
    }
}
