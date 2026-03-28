package xyz.zlatanov.frakkintoasters;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xyz.zlatanov.frakkintoasters.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.board.PegasusBoard;
import xyz.zlatanov.frakkintoasters.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.ship.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static xyz.zlatanov.frakkintoasters.DestinationCard.*;
import static xyz.zlatanov.frakkintoasters.Location.*;
import static xyz.zlatanov.frakkintoasters.SkillCardType.*;

@Getter
@Accessors(fluent = true)
public class Game {
    private ObjectiveCard         objective;
    private GalacticaBoard        galacticaBoard          = new GalacticaBoard();
    private PegasusBoard          pegasusBoard            = new PegasusBoard();
    private CylonFleetBoard       cylonFleetBoard         = new CylonFleetBoard();
    private Deck<CivilianShip>    civilianShipsDeck       = new Deck<>();
    private Deck<BasestarDamage>  basestarDamageTokenDeck = new Deck<>();
    private Deck<PegasusDamage>   pegasusDamageDeck       = new Deck<>();
    private Deck<GalacticaDamage> galacticaDamageDeck     = new Deck<>();
    private Deck<DestinationCard> destinationDeck         = new Deck<>();
    private Deck<SkillCard>       politicsDeck            = new Deck<>();
    private Deck<SkillCard>       leadershipDeck          = new Deck<>();
    private Deck<SkillCard>       tacticsDeck             = new Deck<>();
    private Deck<SkillCard>       pilotingDeck            = new Deck<>();
    private Deck<SkillCard>       engineeringsDeck        = new Deck<>();
    private Deck<SkillCard>       treacheryDeck           = new Deck<>();
    private Deck<QuorumCard>      quorumDeck              = new Deck<>();
    private Deck<CrisisCard>      crisisDeck              = new Deck<>();
    private int                   nukes                   = 3;
    private int                   basestars               = 2;
    private int                   centurions              = 4;
    @Setter
    private Character             president;
    @Setter
    private Character             admiral;
    @Setter
    private Character             cag;


    public void objective(ObjectiveCard objective) {
        if (this.objective != null) {
            throw new FrakCallTheAdmiralException();
        }
        this.objective = objective;
    }

    public void setupCivilianShipsDeck() {
        IntStream.range(0, 2).forEach(i -> civilianShipsDeck.add(new CivilianShip(0, 0, 0)));
        IntStream.range(0, 2).forEach(i -> civilianShipsDeck.add(new CivilianShip(0, 0, 2)));
        IntStream.range(0, 6).forEach(i -> civilianShipsDeck.add(new CivilianShip(0, 0, 1)));
        civilianShipsDeck.add(new CivilianShip(0, 1, 1));
        civilianShipsDeck.add(new CivilianShip(1, 0, 1));
        civilianShipsDeck.shuffle();
    }

    public void setupGalacticaBoard() {
        galacticaBoard.addToReserves(List.of(new Viper(), new Viper(), new Viper(), new Viper(), new Raptor(), new Raptor(), new Raptor(), new Raptor(), new AssaultRaptor()));
        galacticaBoard.addToDamagedShips(List.of(new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII(), new ViperMarkVII()));
        galacticaBoard.place(GALACTICA_SPACE_4_OCLOCK, new Viper());
        galacticaBoard.place(GALACTICA_SPACE_6_OCLOCK, new Viper());
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, List.of(civilianShipsDeck.draw(), civilianShipsDeck.draw()));
        basestars--;
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, new Basestar());
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, List.of(new Raider(), new Raider(), new Raider()));
    }

    public void setupExtraTokens() {
        basestarDamageTokenDeck.add(Arrays.asList(BasestarDamage.values()));
        basestarDamageTokenDeck.shuffle();
        pegasusDamageDeck.add(Arrays.asList(PegasusDamage.values()));
        pegasusDamageDeck.shuffle();
        galacticaDamageDeck.add(Arrays.asList(GalacticaDamage.values()));
        galacticaDamageDeck.shuffle();
        destinationDeck.add(Arrays.asList(DestinationCard.values()));
        //add repeated cards. todo: DOUBLE CHECK!!!
        destinationDeck.add(List.of(REMOTE_PLANET, ICY_MOON, BARREN_PLANET, TYLIUM_PLANET, TYLIUM_PLANET, TYLIUM_PLANET));
        destinationDeck.shuffle();
        quorumDeck.add(Arrays.asList(QuorumCard.values()));
        quorumDeck.shuffle();
        crisisDeck.add(Arrays.asList(CrisisCard.values()));
        crisisDeck.shuffle();
    }

    public void setupSkillCards() {
        addCards(8, 1, CONSOLIDATE_POWER, politicsDeck);
        addCards(6, 2, CONSOLIDATE_POWER, politicsDeck);
        addCards(4, 3, INVESTIGATIVE_COMMITTEE, politicsDeck);
        addCards(2, 4, INVESTIGATIVE_COMMITTEE, politicsDeck);
        addCards(1, 5, INVESTIGATIVE_COMMITTEE, politicsDeck);
        addCards(1, 1, SUPPORT_THE_PEOPLE, politicsDeck);
        addCards(1, 2, SUPPORT_THE_PEOPLE, politicsDeck);
        addCards(1, 3, PREVENTIVE_POLICY, politicsDeck);
        addCards(1, 4, PREVENTIVE_POLICY, politicsDeck);
        addCards(1, 5, PREVENTIVE_POLICY, politicsDeck);
        addCards(1, 6, POLITICAL_PROWESS, politicsDeck);
        //todo check correct quantities of the following
        addCards(1, 6, FORCE_THEIR_HAND, politicsDeck);
        addCards(1, 6, POPULAR_INFLUENCE, politicsDeck);
        addCards(1, 6, NEGOTIATION, politicsDeck);
        politicsDeck.shuffle();

        addCards(8, 1, EXECUTIVE_ORDER, leadershipDeck);
        addCards(6, 2, EXECUTIVE_ORDER, leadershipDeck);
        addCards(4, 3, DECLARE_EMERGENCY, leadershipDeck);
        addCards(2, 4, DECLARE_EMERGENCY, leadershipDeck);
        addCards(1, 5, DECLARE_EMERGENCY, leadershipDeck);
        addCards(1, 1, MAJOR_VICTORY, leadershipDeck);
        addCards(1, 2, MAJOR_VICTORY, leadershipDeck);
        addCards(1, 3, AT_ANY_COST, leadershipDeck);
        addCards(1, 4, AT_ANY_COST, leadershipDeck);
        addCards(1, 5, AT_ANY_COST, leadershipDeck);
        addCards(3, 0, IRON_WILL, leadershipDeck);
        addCards(1, 6, STATE_OF_EMERGENCY, leadershipDeck);
        //todo check correct quantities of the following
        addCards(1, 0, ALL_HANDS_ON_DECK, leadershipDeck);
        addCards(1, 3, RESTORE_ORDER, leadershipDeck);
        addCards(1, 5, CHANGE_OF_PLANS, leadershipDeck);
        leadershipDeck.shuffle();

        addCards(8, 1, LAUNCH_SCOUT, tacticsDeck);
        addCards(6, 2, LAUNCH_SCOUT, tacticsDeck);
        addCards(4, 3, STRATEGIC_PLANNING, tacticsDeck);
        addCards(2, 4, STRATEGIC_PLANNING, tacticsDeck);
        addCards(1, 5, STRATEGIC_PLANNING, tacticsDeck);
        addCards(1, 1, GUTS_AND_INITIATIVE, tacticsDeck);
        addCards(1, 2, GUTS_AND_INITIATIVE, tacticsDeck);
        addCards(1, 3, CRITICAL_SITUATION, tacticsDeck);
        addCards(1, 4, CRITICAL_SITUATION, tacticsDeck);
        addCards(1, 5, CRITICAL_SITUATION, tacticsDeck);
        addCards(3, 0, TRUST_INSTINCTS, tacticsDeck);
        addCards(1, 6, SCOUTING_FOR_FUEL, tacticsDeck);
        //todo check quantities below
        addCards(1, 1, QUICK_THINKING, tacticsDeck);
        addCards(1, 3, UNORTHODOX_PLAN, tacticsDeck);
        addCards(1, 5, A_SECOND_CHANCE, tacticsDeck);
        tacticsDeck.shuffle();

        addCards(8, 1, EVASIVE_MANOEUVRES, pilotingDeck);
        addCards(6, 2, EVASIVE_MANOEUVRES, pilotingDeck);
        addCards(4, 3, MAXIMUM_FIREPOWER, pilotingDeck);
        addCards(2, 4, MAXIMUM_FIREPOWER, pilotingDeck);
        addCards(1, 5, MAXIMUM_FIREPOWER, pilotingDeck);
        addCards(1, 1, FULL_THROTTLE, pilotingDeck);
        addCards(1, 2, FULL_THROTTLE, pilotingDeck);
        addCards(1, 3, RUN_INTERFERENCE, pilotingDeck);
        addCards(1, 4, RUN_INTERFERENCE, pilotingDeck);
        addCards(1, 5, RUN_INTERFERENCE, pilotingDeck);
        addCards(3, 0, PROTECT_THE_FLEET, pilotingDeck);
        addCards(1, 6, BEST_OF_THE_BEST, pilotingDeck);
        //todo check quantities below
        addCards(1, 0, DOGFIGHT, pilotingDeck);
        addCards(1, 3, COMBAT_VETERAN, pilotingDeck);
        addCards(1, 5, LAUNCH_RESERVES, pilotingDeck);
        pilotingDeck.shuffle();

        addCards(8, 1, REPAIR, engineeringsDeck);
        addCards(6, 2, REPAIR, engineeringsDeck);
        addCards(4, 3, SCIENTIFIC_RESEARCH, engineeringsDeck);
        addCards(2, 4, SCIENTIFIC_RESEARCH, engineeringsDeck);
        addCards(1, 5, SCIENTIFIC_RESEARCH, engineeringsDeck);
        addCards(1, 1, JURY_RIGGED, engineeringsDeck);
        addCards(1, 2, JURY_RIGGED, engineeringsDeck);
        addCards(1, 3, CALCULATIONS, engineeringsDeck);
        addCards(1, 4, CALCULATIONS, engineeringsDeck);
        addCards(1, 5, CALCULATIONS, engineeringsDeck);
        addCards(3, 0, ESTABLISH_NETWORK, engineeringsDeck);
        addCards(1, 6, BUILD_NUKE, engineeringsDeck);
        // todo check quantities below
        addCards(1, 0, INSTALL_UPGRADE, engineeringsDeck);
        addCards(1, 3, RAPTOR_SPECIALIST, engineeringsDeck);
        addCards(1, 5, TEST_THE_LIMITS, engineeringsDeck);
        engineeringsDeck.shuffle();

        //todo check quantities below
        addCards(1, 0, DRAIDIS_CONTACT, treacheryDeck);
        addCards(1, 0, BAIT, treacheryDeck);
        addCards(1, 3, A_BETTER_MACHINE, treacheryDeck);
        addCards(1, 3, PERSONAL_VICES, treacheryDeck);
        addCards(1, 4, VIOLENT_OUTBURSTS, treacheryDeck);
        addCards(1, 5, EXPLOIT_A_WEAKNESS, treacheryDeck);
        treacheryDeck.shuffle();
    }

    private static void addCards(int quantity, int value, SkillCardType type, Deck<SkillCard> deck) {
        for (int i = 0; i < quantity; i++) {
            deck.add(new SkillCard(value, type));
        }
    }

}
