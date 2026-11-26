import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { REPOSITORY_URL, SiteFrame } from './Site';

// the server's answers by path: who is logged in (null: nobody) and whether the dev login is on
function stubServer(me: { id: string; name: string } | null, devLogin: boolean) {
  vi.stubGlobal(
    'fetch',
    vi.fn(async (path: string) => {
      if (path === '/api/me') {
        return me ? new Response(JSON.stringify({ ...me, settings: {} }), { status: 200 }) : new Response('{"error":"Unauthorized"}', { status: 401 });
      }
      return new Response(JSON.stringify({ providers: [], devLogin }), { status: 200 });
    }),
  );
}

function renderSite() {
  return render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter>
        <SiteFrame>
          <p>the page</p>
        </SiteFrame>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

afterEach(() => vi.unstubAllGlobals());

describe('SiteFrame', () => {
  it('links the brand home and the pages, and offers the login to nobody logged in', async () => {
    stubServer(null, false);
    renderSite();

    expect(await screen.findByRole('link', { name: 'Log in' })).toHaveAttribute('href', '/login');
    expect(screen.getByRole('link', { name: 'Frakkin Toasters' })).toHaveAttribute('href', '/');
    for (const [name, href] of [
      ['Play', '/play'],
      ['Users', '/users'],
    ]) {
      expect(screen.getByRole('link', { name })).toHaveAttribute('href', href);
    }
    expect(screen.getByText('the page')).toBeInTheDocument();
  });

  it("opens the user's menu: the profile, another user in this tab and the logout", async () => {
    stubServer({ id: 'u1', name: 'Starbuck' }, true);
    renderSite();

    const button = await screen.findByRole('button', { name: /Starbuck/ });
    expect(screen.queryByRole('link', { name: 'Log in' })).not.toBeInTheDocument();
    fireEvent.click(button);
    expect(button).toHaveAttribute('aria-expanded', 'true');
    expect(screen.getByRole('link', { name: 'Profile' })).toHaveAttribute('href', '/profile');
    expect(await screen.findByRole('button', { name: 'Another user in this tab' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Log out' })).toBeInTheDocument();

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('link', { name: 'Profile' })).not.toBeInTheDocument();
  });

  it('runs the copyright from 2025 to this year, and links the about page, the repository and the contact', async () => {
    stubServer(null, false);
    renderSite();

    const footer = screen.getByRole('contentinfo');
    const year = new Date().getFullYear();
    expect(footer).toHaveTextContent(year > 2025 ? `© 2025 – ${year}` : '© 2025');
    expect(screen.getAllByRole('link', { name: 'About' }).map((link) => link.getAttribute('href'))).toContain('/about');
    expect(screen.getByRole('link', { name: 'GitHub' })).toHaveAttribute('href', REPOSITORY_URL);
    expect(screen.getByRole('link', { name: 'Contact' })).toHaveAttribute('href', '#');
    await screen.findByRole('link', { name: 'Log in' });
  });
});
