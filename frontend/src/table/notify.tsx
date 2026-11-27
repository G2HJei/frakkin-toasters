import { useEffect, useRef, useState } from 'react';

const FLASH_EVERY = 1000;

function notificationsSupported(): boolean {
  return typeof window !== 'undefined' && 'Notification' in window;
}

/**
 * Nothing while the tab is in front, and nothing in a sandbox, whose user is always asked. The favicon changes too, see
 * `TablePage`.
 */
export function useYourMoveNotice(yourMove: boolean, title: string, body: string | null, tag: string, sandbox: boolean) {
  const was = useRef(yourMove);
  const [hidden, setHidden] = useState(() => typeof document !== 'undefined' && document.visibilityState === 'hidden');

  useEffect(() => {
    const onVisibility = () => setHidden(document.visibilityState === 'hidden');
    document.addEventListener('visibilitychange', onVisibility);
    return () => document.removeEventListener('visibilitychange', onVisibility);
  }, []);

  // the notification, once per new move, while the tab is hidden
  useEffect(() => {
    const became = yourMove && !was.current;
    was.current = yourMove;
    if (!became || sandbox || document.visibilityState !== 'hidden' || !notificationsSupported() || Notification.permission !== 'granted') {
      return;
    }
    try {
      const notification = new Notification(`Your move · ${title}`, { body: body ?? undefined, tag, icon: '/favicon-move.svg' });
      notification.onclick = () => {
        window.focus();
        notification.close();
      };
    } catch {
      // a browser that only notifies from a service worker: the title still flashes
    }
  }, [yourMove, title, body, tag, sandbox]);

  // the title flashes while the tab is hidden and the move waits
  useEffect(() => {
    if (!yourMove || !hidden || sandbox) {
      return;
    }
    let on = false;
    const timer = window.setInterval(() => {
      on = !on;
      document.title = on ? '▶ Your move' : `● ${title}`;
    }, FLASH_EVERY);
    return () => {
      window.clearInterval(timer);
      document.title = `${yourMove ? '● ' : ''}${title}`;
    };
  }, [yourMove, hidden, title, sandbox]);
}

/**
 * Asks the browser to allow the notifications, on a click: never on load. Shown only while the user hasn't decided.
 */
export function NotifyButton() {
  const [permission, setPermission] = useState(() => (notificationsSupported() ? Notification.permission : 'denied'));
  if (permission !== 'default') {
    return null;
  }
  return (
    <button
      type="button"
      className="link notify-ask"
      title="Get a browser notification when it's your move while this tab is in the background"
      onClick={() => {
        void Notification.requestPermission().then(setPermission);
      }}
    >
      Notify me
    </button>
  );
}
