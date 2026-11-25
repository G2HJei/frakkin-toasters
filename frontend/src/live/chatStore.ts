import { create } from 'zustand';
import type { ChatLine, ServerMessage } from '../wire/types';

export interface ChatState {
  // the messages of the channels the viewer reads, oldest first, each once
  messages: ChatLine[];
  // why the server refused the viewer's last message, until the next one goes out
  rejected: string | null;
  receive(message: ServerMessage): void;
  clearRejected(): void;
  reset(): void;
}

/**
 * The chat of the open table, fed by the socket: the last messages on a new connection, then each new one. A message
 * that came both ways (it was written while the connection opened) is kept once.
 */
export const useChatStore = create<ChatState>((set) => ({
  messages: [],
  rejected: null,
  receive: (message) =>
    set((state) => {
      switch (message.type) {
        case 'chatLog':
          return { messages: merge(message.messages ?? [], state.messages) };
        case 'chat':
          return message.message ? { messages: merge(state.messages, [message.message]) } : {};
        case 'chatRejected':
          return { rejected: message.message ?? 'The message was refused' };
        default:
          return {};
      }
    }),
  clearRejected: () => set({ rejected: null }),
  reset: () => set({ messages: [], rejected: null }),
}));

export function isChatMessage(message: ServerMessage): boolean {
  return message.type === 'chat' || message.type === 'chatLog' || message.type === 'chatRejected';
}

function merge(older: ChatLine[], newer: ChatLine[]): ChatLine[] {
  const byId = new Map<number, ChatLine>();
  for (const line of [...older, ...newer]) {
    byId.set(line.id, line);
  }
  return [...byId.values()].sort((a, b) => a.id - b.id);
}
