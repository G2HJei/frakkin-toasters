package xyz.zlatanov.frakkintoasters.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
class SchemaTest {

    @Autowired
    JdbcClient jdbc;

    @Test
    void theTablesHaveTheColumnsOfThePlan() {
        assertEquals(List.of("id uuid NO", "name text NO", "settings jsonb NO"), columns("app_user"));
        assertEquals(List.of("provider text NO", "subject text NO", "user_id uuid NO"), columns("user_identity"));
        assertEquals(List.of(
                "id uuid NO",
                "name text NO",
                "status text NO",
                "turn integer NO",
                "host_user_id uuid NO",
                "winner_team text YES",
                "waiting_for_player_number integer YES",
                "created_at timestamp with time zone NO",
                "updated_at timestamp with time zone NO",
                "format_version integer NO",
                "mode text NO",
                "seed bigint YES"), columns("game_bundle"));
        assertEquals(List.of("game_bundle_id uuid NO", "user_id uuid NO", "player_number integer YES"),
                columns("participant"));
        assertEquals(List.of("game_bundle_id uuid NO", "sequence integer NO", "entry jsonb NO"),
                columns("journal_entry"));
    }

    @Test
    void theKeysAreThoseOfThePlan() {
        assertEquals(List.of(
                        "app_user PRIMARY KEY id",
                        "game_bundle PRIMARY KEY id",
                        "journal_entry PRIMARY KEY game_bundle_id,sequence",
                        "participant PRIMARY KEY game_bundle_id,user_id",
                        "participant UNIQUE game_bundle_id,player_number",
                        "user_identity PRIMARY KEY provider,subject"),
                jdbc.sql("""
                                SELECT tc.table_name || ' ' || tc.constraint_type || ' '
                                       || string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position)
                                FROM information_schema.table_constraints tc
                                JOIN information_schema.key_column_usage kcu ON kcu.constraint_name = tc.constraint_name
                                WHERE tc.table_schema = 'public' AND tc.constraint_type IN ('PRIMARY KEY', 'UNIQUE')
                                  AND tc.table_name IN ('app_user', 'user_identity', 'game_bundle', 'participant', 'journal_entry')
                                GROUP BY tc.table_name, tc.constraint_type, tc.constraint_name
                                ORDER BY 1""")
                        .query(String.class)
                        .list());
        assertEquals(List.of(
                        "game_bundle.host_user_id -> app_user",
                        "journal_entry.game_bundle_id -> game_bundle",
                        "participant.game_bundle_id -> game_bundle",
                        "participant.user_id -> app_user",
                        "user_identity.user_id -> app_user"),
                jdbc.sql("""
                                SELECT tc.table_name || '.' || kcu.column_name || ' -> ' || ccu.table_name
                                FROM information_schema.table_constraints tc
                                JOIN information_schema.key_column_usage kcu ON kcu.constraint_name = tc.constraint_name
                                JOIN information_schema.constraint_column_usage ccu
                                     ON ccu.constraint_name = tc.constraint_name
                                WHERE tc.table_schema = 'public' AND tc.constraint_type = 'FOREIGN KEY'
                                  AND tc.table_name IN ('app_user', 'user_identity', 'game_bundle', 'participant', 'journal_entry')
                                ORDER BY 1""")
                        .query(String.class)
                        .list());
    }

    private List<String> columns(String table) {
        return jdbc.sql("""
                        SELECT column_name || ' ' || data_type || ' ' || is_nullable
                        FROM information_schema.columns
                        WHERE table_schema = 'public' AND table_name = :table
                        ORDER BY ordinal_position""")
                .param("table", table)
                .query(String.class)
                .list();
    }
}
