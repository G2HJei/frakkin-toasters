-- A sandbox is played by one user for every seat, sees everything and can undo. A seeded game reproduces its random
-- results from the seed, also after a restore. A standard game has no seed.
ALTER TABLE game_bundle
    ADD COLUMN mode text NOT NULL DEFAULT 'STANDARD' CHECK (mode IN ('STANDARD', 'SANDBOX')),
    ADD COLUMN seed bigint;
