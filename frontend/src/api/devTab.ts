// The dev profile's login per browser tab, so one browser plays several users side by side: the tab keeps its user's
// name in sessionStorage (each tab has its own), sends it with every request as `X-Dev-User` and with its game
// socket as `?devUser=`, and a game tab it opens gets `?as=` so it is the same user. Only the dev profile's server
// reads them. Storage can be off: the tab then has no user of its own.

const KEY = 'frakkin:devUser';

export function tabUser(): string | null {
  try {
    return window.sessionStorage.getItem(KEY);
  } catch {
    return null;
  }
}

export function setTabUser(name: string | null): void {
  try {
    if (name) {
      window.sessionStorage.setItem(KEY, name);
    } else {
      window.sessionStorage.removeItem(KEY);
    }
  } catch {
    // no user of its own, see above
  }
}

export function withTabUser(path: string): string {
  const name = tabUser();
  return name ? `${path}${path.includes('?') ? '&' : '?'}as=${encodeURIComponent(name)}` : path;
}

// A tab opened with `?as=` is that user's from the start.
if (typeof window !== 'undefined') {
  const as = new URLSearchParams(window.location.search).get('as');
  if (as) {
    setTabUser(as);
  }
}
