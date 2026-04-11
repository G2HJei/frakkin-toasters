package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.LEADERSHIP;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;

@ExtendWith(MockitoExtension.class)
class ReceiveSkillCardsEventTest {

    @SuppressWarnings("unchecked")
    Deck<SkillCard> leadershipDeck = mock(Deck.class);
    @SuppressWarnings("unchecked")
    Deck<SkillCard> treacheryDeck  = mock(Deck.class);
    SkillCard leadershipCard = new SkillCard(0, AT_ANY_COST);
    SkillCard treacheryCard  = new SkillCard(0, BAIT);
    Game      game           = Game.builder(4)
            .decks(DecksHolder.builder()
                    .leadership(leadershipDeck)
                    .treachery(treacheryDeck)
                    .build()).build();

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldReceiveCardsWithinSkillSet() {
        when(leadershipDeck.draw()).thenReturn(leadershipCard);
        new ReceiveSkillsEvent(1, Map.of(LEADERSHIP, 1)).execute(game);
        assertEquals(List.of(leadershipCard), game.player(1).skillCards().cards());
    }

    @Test
    void shouldNotReceiveOutsideOfSkillSet() {
        val action = new ReceiveSkillsEvent(1, Map.of(TREACHERY, 1));
        assertFalse(action.isValid(game));
    }

    @Test
    void shouldReceiveAnyColorWhenRevealedCylon() {
        revealCylon();
        when(leadershipDeck.draw()).thenReturn(leadershipCard);
        when(treacheryDeck.draw()).thenReturn(treacheryCard);

        new ReceiveSkillsEvent(1, Map.of(LEADERSHIP, 1, TREACHERY, 1)).execute(game);

        assertEquals(List.of(leadershipCard, treacheryCard), game.player(1).skillCards().cards());
    }

    @Test
    void shouldNotAllowDoubleSelectionForRevealedCylon() {
        revealCylon();
        assertFalse(new ReceiveSkillsEvent(1, Map.of(TREACHERY, 2)).isValid(game));
    }

    private void revealCylon() {
        game.player(1).loyaltyCards().add(CYLON_SEND_TO_BRIG);
        game.player(1).loyaltyCards().reveal(CYLON_SEND_TO_BRIG);
    }
}