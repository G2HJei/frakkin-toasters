import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { LoginPage } from './LoginPage';

const CODE = '042317';

// nobody logged in, Google, Discord and the email login on; a code request answers with `codeAnswer`
function stubServer(codeAnswer: Response = new Response(null, { status: 204 })) {
  const requests: { path: string; body: unknown }[] = [];
  vi.stubGlobal(
    'fetch',
    vi.fn(async (path: string, init?: RequestInit) => {
      const body = typeof init?.body === 'string' ? JSON.parse(init.body) : undefined;
      requests.push({ path, body });
      if (path === '/api/me') {
        return new Response('{"error":"Unauthorized"}', { status: 401 });
      }
      if (path === '/api/login/email') {
        return codeAnswer.clone();
      }
      if (path === '/api/login/email/verify') {
        return body.code === CODE
          ? new Response(JSON.stringify({ id: 'u1', name: 'Pilot 1234', email: body.email, settings: {} }), { status: 200 })
          : new Response('{"message":"The code is wrong or has expired"}', { status: 400 });
      }
      return new Response(JSON.stringify({ providers: ['google', 'discord'], devLogin: false, email: true }), { status: 200 });
    }),
  );
  return requests;
}

function renderLogin(entry = '/login') {
  return render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter initialEntries={[entry]}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/play" element={<p>the games</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

afterEach(() => vi.unstubAllGlobals());

describe('LoginPage', () => {
  it("leads each provider's button with its mark once the mark loads", async () => {
    stubServer();
    renderLogin();

    const google = await screen.findByRole('link', { name: 'Log in with Google' });
    expect(google).toHaveAttribute('href', '/oauth2/authorization/google');
    expect(screen.getByRole('link', { name: 'Log in with Discord' })).toHaveAttribute('href', '/oauth2/authorization/discord');
    const icon = within(google).getByTestId('google-icon');
    expect(icon).toHaveClass('pending');
    const image = icon.querySelector('img')!;
    expect(image).toHaveAttribute('src', '/icons/google.svg');
    fireEvent.load(image);
    expect(icon).not.toHaveClass('pending');
    expect(screen.getByText('or')).toBeInTheDocument();
  });

  it('logs in with the code sent to the email', async () => {
    const requests = stubServer();
    const user = userEvent.setup();
    renderLogin();

    await user.type(await screen.findByRole('textbox', { name: 'email' }), ' kara@example.com ');
    await user.click(screen.getByRole('button', { name: 'Email me a code' }));
    expect(await screen.findByText('kara@example.com')).toBeInTheDocument();
    expect(requests.find((request) => request.path === '/api/login/email')?.body).toEqual({ email: 'kara@example.com' });

    const code = screen.getByRole('textbox', { name: 'code' });
    const logIn = screen.getByRole('button', { name: 'Log in' });
    await user.type(code, '12-34');
    expect(code).toHaveValue('1234'); // digits only
    expect(logIn).toBeDisabled();
    await user.type(code, '56');
    await user.click(logIn);
    expect(await screen.findByText('The code is wrong or has expired')).toBeInTheDocument();

    await user.clear(code);
    await user.type(code, CODE);
    await user.click(logIn);
    expect(await screen.findByText('the games')).toBeInTheDocument();
    expect(requests.find((request) => request.path === '/api/login/email/verify' && (request.body as { code: string }).code === CODE)?.body).toEqual({
      email: 'kara@example.com',
      code: CODE,
    });
  });

  it("shows why the server didn't send a code", async () => {
    stubServer(new Response('{"message":"A code is on its way: wait a minute before asking for another"}', { status: 429 }));
    const user = userEvent.setup();
    renderLogin();

    await user.type(await screen.findByRole('textbox', { name: 'email' }), 'kara@example.com');
    await user.click(screen.getByRole('button', { name: 'Email me a code' }));

    expect(await screen.findByText('A code is on its way: wait a minute before asking for another')).toBeInTheDocument();
    expect(screen.queryByRole('textbox', { name: 'code' })).not.toBeInTheDocument();
  });

  it('says why a provider login came back without logging in', async () => {
    stubServer();
    renderLogin('/login?error=unverified-email');

    expect(await screen.findByText(/email address isn't verified/)).toBeInTheDocument();
  });
});
