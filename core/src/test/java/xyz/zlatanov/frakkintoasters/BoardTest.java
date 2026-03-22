package xyz.zlatanov.frakkintoasters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void shouldCreateBoard() {
        assertDoesNotThrow(Board::new);
    }
}