package xyz.zlatanov.frakkintoasters.event.officials;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.ArrayList;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

public class DetermineNextCagEventProcessor extends EventProcessor<DetermineNextCagEvent> implements LineOfSuccessionSelector {

    @Override
    public List<Character> lineOfSuccession() {
        val lineOfSuccession = new ArrayList<>(List.of(
                LEE_APOLLO_ADAMA,
                KARA_STARBUCK_THRACE,
                LOUANNE_KAT_KATRAINE,
                KARL_HELO_AGATHON_ALT,
                SHARON_BOOMER_VALERII,
                BRENDAN_HOTDOG_COSTANZA,
                SAMUEL_T_ANDERS,
                LEE_ADAMA,
                KARL_HELO_AGATHON,
                WILLIAM_ADAMA,
                HELENA_CAIN,
                SAUL_TIGH,
                FELIX_GAETA,
                ANASTASIA_DEE_DUALLA,
                LOUIS_HOSHI,
                TOM_ZAREK_ALT,
                CHIEF_GALEN_TYROL,
                CALLANDRA_CALLY_TYROL,
                SHERMAN_DOC_COTTLE,
                TOM_ZAREK,
                ELLEN_TIGH,
                GAIUS_BALTAR_ALT,
                GAIUS_BALTAR,
                TORY_FOSTER,
                ROMO_LAMPKIN,
                LAURA_ROSLIN));
        lineOfSuccession.removeAll(galacticaBoard.charactersIn(BRIG));
        return lineOfSuccession;
    }

    @Override
    public Followup process() {
        game.cag(calcNextInLine(game));
        return Followup.NONE;
    }

    @Override
    public Character noNextInLine() {
        return null;
    }
}
