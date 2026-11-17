package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.event.Event;

/**
 * A scenario setup of the sandbox: it puts components where a test by hand needs them. The engine injects it ahead of
 * the pending input ({@code GameEngine.inject}), and never takes one as an answer to a decision. The backend refuses
 * it in any game but a sandbox.
 */
public interface SandboxEvent extends Event {
}
