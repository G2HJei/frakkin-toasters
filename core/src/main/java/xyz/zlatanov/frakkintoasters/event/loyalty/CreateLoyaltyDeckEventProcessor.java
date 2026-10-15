package xyz.zlatanov.frakkintoasters.event.loyalty;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.DeckView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Collection;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class CreateLoyaltyDeckEventProcessor extends EventProcessor<CreateLoyaltyDeckEvent> {

    @Override
    public Followup process() {
        creteLoyaltyDecks();
        dealLoyaltyCards();
        dealMotiveCards();
        return followup();
    }

    private void creteLoyaltyDecks() {
        val cylonLeader = cylonLeader();
        val notCylonDraws = notCylonDraws(game.players().size(), cylonLeader) + extraCards();
        val cylonDraws = cylonDraws(game.players().size(), cylonLeader);
        ops.loyaltyDeck()
                .createNotACylon()
                .create(notCylonDraws, cylonDraws, mutineer());
    }

    private void dealLoyaltyCards() {
        for (val player : game.players()) {
            val cardsToDraw = player.character() == GAIUS_BALTAR ? 2 : 1;
            ops.player(player.number()).drawLoyaltyCards(cardsToDraw);
        }
    }

    private void dealMotiveCards() {
        if (!cylonLeader()) {
            return;
        }
        val cylonPlayerNumber = game.players()
                .stream()
                .filter(p -> p.character().type() == CYLON_LEADER)
                .map(PlayerView::number)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
        ops.player(cylonPlayerNumber).drawMotiveCards();
    }

    private Followup followup() {
        val hasMutineer = game.players()
                .stream()
                .map(PlayerView::loyaltyCards)
                .map(DeckView::cards)
                .flatMap(Collection::stream)
                .anyMatch(MUTINEER::equals);
        return hasMutineer
                ? single(new RevealMutineerEvent())
                : Followup.NONE;
    }

    private boolean cylonLeader() {
        return getSelectedCharacters()
                .stream()
                .anyMatch(c -> c.type() == CYLON_LEADER);
    }

    private boolean mutineer() {
        val playerCount = game.players().size();
        val hasCylonLeader = cylonLeader();
        return playerCount == 4 && !hasCylonLeader ||
                playerCount == 5 && hasCylonLeader ||
                playerCount == 6 && !hasCylonLeader ||
                playerCount == 7;
    }

    private int extraCards() {
        val selectedCharacters = getSelectedCharacters();
        var extraCards = 0;
        if (selectedCharacters.contains(SHARON_BOOMER_VALERII)) {
            extraCards++;
        }
        if (selectedCharacters.contains(GAIUS_BALTAR)) {
            extraCards++;
        }
        return extraCards;
    }

    private List<Character> getSelectedCharacters() {
        return game.players()
                .stream()
                .map(PlayerView::character)
                .toList();
    }

    private static int notCylonDraws(int playerCount, boolean hasCylonLeader) {
        if (playerCount == 3
                || playerCount == 4 && hasCylonLeader) {
            return 6;
        } else if (playerCount == 4
                || playerCount == 5 && hasCylonLeader) {
            return 8;
        } else if (playerCount == 5
                || playerCount == 6 && hasCylonLeader) {
            return 9;
        } else {
            return 11;
        }
    }

    private static int cylonDraws(int playerCount, boolean hasCylonLeader) {
        return playerCount < 5 ||
                playerCount == 5 && hasCylonLeader
                ? 1
                : 2;
    }
}
