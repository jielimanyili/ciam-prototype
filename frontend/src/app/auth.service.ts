import { Injectable, signal } from '@angular/core';
import { UserManager, UserManagerSettings, type User } from 'oidc-client-ts';

const oidcSettings: UserManagerSettings = {
  authority: 'http://localhost:8080/realms/ciam-demo',
  client_id: 'ciam-web',
  redirect_uri: window.location.origin + '/',
  post_logout_redirect_uri: window.location.origin + '/',
  response_type: 'code',
  scope: 'openid profile',
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private manager = new UserManager(oidcSettings);
  readonly user = signal<User | null>(null);
  readonly error = signal('');

  constructor() {
    if (window.location.search.includes('code=')) {
      // Redirect back from the IdP — complete Authorization Code + PKCE.
      this.manager
        .signinRedirectCallback()
        .then((u) => {
          this.user.set(u);
          window.history.replaceState({}, document.title, '/');
        })
        .catch((e) => this.error.set((e as Error).message));
    } else {
      this.manager
        .getUser()
        .then((u) => this.user.set(u))
        .catch(() => this.user.set(null));
    }
  }

  async login(): Promise<void> {
    try {
      await this.manager.signinRedirect();
    } catch (e) {
      this.error.set((e as Error).message);
    }
  }

  async logout(): Promise<void> {
    try {
      await this.manager.signoutRedirect();
    } catch (e) {
      this.error.set((e as Error).message);
    }
  }

  accessToken(): string {
    return this.user()?.access_token ?? '';
  }
}
