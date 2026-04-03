package xyz.zlatanov.frakkintoasters.action.skills;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;

@ExtendWith(MockitoExtension.class)
class ReceiveSkillCardsActionTest {

    Deck<SkillCard> leadershipDeck = mock(Deck.class);
    Deck<SkillCard> treacheryDeck  = mock(Deck.class);
    SkillCard       leadershipCard = new SkillCard(0, AT_ANY_COST);
    SkillCard       treacheryCard  = new SkillCard(0, BAIT);
    Game            game           = new Game(KOBOL, 4,
            DecksHolder.builder()
                    .leadership(leadershipDeck)
                    .treachery(treacheryDeck)
                    .build());

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldReceiveCardsWithinSkillSet() {
        when(leadershipDeck.draw()).thenReturn(leadershipCard);
        new ReceiveSkillsAction(1, List.of(new SkillSelection(1, TACTICS))).execute(game);
        assertEquals(List.of(leadershipCard), game.player(1).skillCards());
    }

    @Test
    void shouldNotReceiveOutsideOfSkillSet() {
        val skillCardSelection = List.of(new SkillSelection(1, TREACHERY));
        val action = new ReceiveSkillsAction(1, skillCardSelection);
        assertFalse(action.isValid(game));
    }

    @Test
    void shouldReceiveAnyColorWhenRevealedCylon() {
        when(leadershipDeck.draw()).thenReturn(leadershipCard);
        when(treacheryDeck.draw()).thenReturn(treacheryCard);
        game.player(1).loyaltyCards().add(CYLON_SEND_TO_BRIG);
        //game.player(1).revealLoyaltyCard(CYLON_SEND_TO_BRIG);
        //todo
    }

}