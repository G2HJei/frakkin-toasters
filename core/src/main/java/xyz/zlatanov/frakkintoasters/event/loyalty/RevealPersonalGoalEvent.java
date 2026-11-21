package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

// A player reveals one of his Personal Goal Loyalty Cards as his action, while the condition printed on it holds (rule
// 5.3.1). Up to 6 distance he then shuffles a "You Are Not a Cylon" card into the Loyalty deck and draws a new one
// (rule 5.3.1.1).
//
// @param discards the Skill Cards "Selfish" discards to reveal it, worth 20 strength together; empty for the others
public record RevealPersonalGoalEvent(int playerNumber, LoyaltyCard card, List<SkillCard> discards) implements ActionEvent {
}
