package xyz.zlatanov.frakkintoasters.state.util;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

public class AllCardsProvider {

    public static Deck<CivilianShip> civilianShipsDeck() {
        val deck = new Deck<CivilianShip>();
        //todo randomize civ ship ids to avoid frontend cheating
        deck.addOnTop(new CivilianShip(2001, 0, 0, 0));
        deck.addOnTop(new CivilianShip(2002, 0, 0, 0));
        deck.addOnTop(new CivilianShip(2003, 0, 0, 2));
        deck.addOnTop(new CivilianShip(2004, 0, 0, 2));
        deck.addOnTop(new CivilianShip(2005, 0, 0, 1));
        deck.addOnTop(new CivilianShip(2006, 0, 0, 1));
        deck.addOnTop(new CivilianShip(2007, 0, 0, 1));
        deck.addOnTop(new CivilianShip(2008, 0, 0, 1));
        deck.addOnTop(new CivilianShip(2009, 0, 0, 1));
        deck.addOnTop(new CivilianShip(2010, 0, 0, 1));
        deck.addOnTop(new CivilianShip(2011, 0, 1, 1));
        deck.addOnTop(new CivilianShip(2012, 1, 0, 1));
        deck.shuffle();
        return deck;
    }

    @SafeVarargs
    public static <T extends Enum<T>> Deck<T> genericDeck(Class<T> clazz, T... repeatedCards) {
        return new Deck<T>()
                .addOnTop(Arrays.asList(clazz.getEnumConstants()))
                .addOnTop(Arrays.asList(repeatedCards))
                .shuffle();
    }

    public static Deck<SkillCard> politicsCards() {
        return new Deck<SkillCard>()
                .addOnTop(skills(8, 1, CONSOLIDATE_POWER))
                .addOnTop(skills(6, 2, CONSOLIDATE_POWER))
                .addOnTop(skills(4, 3, INVESTIGATIVE_COMMITTEE))
                .addOnTop(skills(2, 4, INVESTIGATIVE_COMMITTEE))
                .addOnTop(skills(1, 5, INVESTIGATIVE_COMMITTEE))
                .addOnTop(skills(1, 1, SUPPORT_THE_PEOPLE))
                .addOnTop(skills(1, 2, SUPPORT_THE_PEOPLE))
                .addOnTop(skills(1, 3, PREVENTIVE_POLICY))
                .addOnTop(skills(1, 4, PREVENTIVE_POLICY))
                .addOnTop(skills(1, 5, PREVENTIVE_POLICY))
                .addOnTop(skills(1, 6, POLITICAL_PROWESS))
                //todo check correct quantities of the following
                .addOnTop(skills(1, 6, FORCE_THEIR_HAND))
                .addOnTop(skills(1, 6, POPULAR_INFLUENCE))
                .addOnTop(skills(1, 6, NEGOTIATION))
                .shuffle();
    }

    public static Deck<SkillCard> leadershipCards() {
        return new Deck<SkillCard>()
                .addOnTop(skills(8, 1, EXECUTIVE_ORDER))
                .addOnTop(skills(6, 2, EXECUTIVE_ORDER))
                .addOnTop(skills(4, 3, DECLARE_EMERGENCY))
                .addOnTop(skills(2, 4, DECLARE_EMERGENCY))
                .addOnTop(skills(1, 5, DECLARE_EMERGENCY))
                .addOnTop(skills(1, 1, MAJOR_VICTORY))
                .addOnTop(skills(1, 2, MAJOR_VICTORY))
                .addOnTop(skills(1, 3, AT_ANY_COST))
                .addOnTop(skills(1, 4, AT_ANY_COST))
                .addOnTop(skills(1, 5, AT_ANY_COST))
                .addOnTop(skills(3, 0, IRON_WILL))
                .addOnTop(skills(1, 6, STATE_OF_EMERGENCY))
                //todo check correct quantities of the deck
                .addOnTop(skills(1, 0, ALL_HANDS_ON_DECK))
                .addOnTop(skills(1, 3, RESTORE_ORDER))
                .addOnTop(skills(1, 5, CHANGE_OF_PLANS))
                .shuffle();
    }

    public static Deck<SkillCard> tacticsCards() {
        return new Deck<SkillCard>()
                .addOnTop(skills(8, 1, LAUNCH_SCOUT))
                .addOnTop(skills(6, 2, LAUNCH_SCOUT))
                .addOnTop(skills(4, 3, STRATEGIC_PLANNING))
                .addOnTop(skills(2, 4, STRATEGIC_PLANNING))
                .addOnTop(skills(1, 5, STRATEGIC_PLANNING))
                .addOnTop(skills(1, 1, GUTS_AND_INITIATIVE))
                .addOnTop(skills(1, 2, GUTS_AND_INITIATIVE))
                .addOnTop(skills(1, 3, CRITICAL_SITUATION))
                .addOnTop(skills(1, 4, CRITICAL_SITUATION))
                .addOnTop(skills(1, 5, CRITICAL_SITUATION))
                .addOnTop(skills(3, 0, TRUST_INSTINCTS))
                .addOnTop(skills(1, 6, SCOUTING_FOR_FUEL))
                //todo check quantities below
                .addOnTop(skills(1, 1, QUICK_THINKING))
                .addOnTop(skills(1, 3, UNORTHODOX_PLAN))
                .addOnTop(skills(1, 5, A_SECOND_CHANCE))
                .shuffle();
    }

    public static Deck<SkillCard> pilotingCards() {
        return new Deck<SkillCard>()
                .addOnTop(skills(8, 1, EVASIVE_MANOEUVRES))
                .addOnTop(skills(6, 2, EVASIVE_MANOEUVRES))
                .addOnTop(skills(4, 3, MAXIMUM_FIREPOWER))
                .addOnTop(skills(2, 4, MAXIMUM_FIREPOWER))
                .addOnTop(skills(1, 5, MAXIMUM_FIREPOWER))
                .addOnTop(skills(1, 1, FULL_THROTTLE))
                .addOnTop(skills(1, 2, FULL_THROTTLE))
                .addOnTop(skills(1, 3, RUN_INTERFERENCE))
                .addOnTop(skills(1, 4, RUN_INTERFERENCE))
                .addOnTop(skills(1, 5, RUN_INTERFERENCE))
                .addOnTop(skills(3, 0, PROTECT_THE_FLEET))
                .addOnTop(skills(1, 6, BEST_OF_THE_BEST))
                //todo check quantities below
                .addOnTop(skills(1, 0, DOGFIGHT))
                .addOnTop(skills(1, 3, COMBAT_VETERAN))
                .addOnTop(skills(1, 5, LAUNCH_RESERVES))
                .shuffle();
    }

    public static Deck<SkillCard> engineeringCards() {
        return new Deck<SkillCard>()
                .addOnTop(skills(8, 1, REPAIR))
                .addOnTop(skills(6, 2, REPAIR))
                .addOnTop(skills(4, 3, SCIENTIFIC_RESEARCH))
                .addOnTop(skills(2, 4, SCIENTIFIC_RESEARCH))
                .addOnTop(skills(1, 5, SCIENTIFIC_RESEARCH))
                .addOnTop(skills(1, 1, JURY_RIGGED))
                .addOnTop(skills(1, 2, JURY_RIGGED))
                .addOnTop(skills(1, 3, CALCULATIONS))
                .addOnTop(skills(1, 4, CALCULATIONS))
                .addOnTop(skills(1, 5, CALCULATIONS))
                .addOnTop(skills(3, 0, ESTABLISH_NETWORK))
                .addOnTop(skills(1, 6, BUILD_NUKE))
                // todo check quantities
                .addOnTop(skills(1, 0, INSTALL_UPGRADE))
                .addOnTop(skills(1, 3, RAPTOR_SPECIALIST))
                .addOnTop(skills(1, 5, TEST_THE_LIMITS))
                .shuffle();
    }

    public static Deck<SkillCard> treacheryCards() {
        return new Deck<SkillCard>()
                //todo check quantities below
                .addOnTop(skills(1, 0, DRAIDIS_CONTACT))
                .addOnTop(skills(1, 0, BAIT))
                .addOnTop(skills(1, 3, A_BETTER_MACHINE))
                .addOnTop(skills(1, 3, PERSONAL_VICES))
                .addOnTop(skills(1, 4, VIOLENT_OUTBURSTS))
                .addOnTop(skills(1, 5, EXPLOIT_A_WEAKNESS))
                .shuffle();
    }

    private static List<SkillCard> skills(int quantity, int value, SkillCardType type) {
        val result = new ArrayList<SkillCard>();
        for (int i = 0; i < quantity; i++) {
            result.add(new SkillCard(value, type));
        }
        return result;
    }
}
