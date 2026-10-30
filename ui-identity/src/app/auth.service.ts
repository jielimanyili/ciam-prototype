import { Injectable, inject, signal } from '@angular/core';
import { UserManager, UserManagerSettings, type User } from 'oidc-client-ts';
import { ConfigService } from './config';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly config = inject(ConfigService);
  private manager?: UserManager;
  readonly user = signal<User | null>(null);
  readonly error = signal('');

  private getManager(): UserManager {
    if (!this.manager) {
      const settings: UserManagerSettings = {
        authority: this.config.config().authority,
        client_id: 'ui-identity',
        redirect_uri: window.location.origin + '/',
        post_logout_redirect_uri: window.location.origin + '/',
        response_type: 'code',
        scope: 'openid profile',
      };
      const audience = this.config.config().audience;
      if (audience) {
        // Auth0 needs the target API's audience in the authorize request so the access token
        // carries the right `aud`; Keycloak derives `aud` from its mappers, so this stays empty
        // (and is omitted) locally — provider-agnostic (ADR-007).
        settings.extraQueryParams = { audience };
      }
      this.manager = new UserManager(settings);
    }
    return this.manager;
  }

  constructor() {
    if (window.location.search.includes('code=')) {
      // Redirect back from the IdP — complete Authorization Code + PKCE.
      this.getManager()
        .signinRedirectCallback()
        .then((u) => {
          this.user.set(u);
          window.history.replaceState({}, document.title, '/');
        })
        .catch((e) => this.error.set((e as Error).message));
    } else {
      this.getManager()
        .getUser()
        .then((u) => this.user.set(u))
        .catch(() => this.user.set(null));
    }
  }

  async login(): Promise<void> {
    try {
      await this.getManager().signinRedirect();
    } catch (e) {
      this.error.set((e as Error).message);
    }
  }

  async logout(): Promise<void> {
    try {
      await this.getManager().signoutRedirect();
    } catch (e) {
      this.error.set((e as Error).message);
    }
  }

  accessToken(): string {
    return this.user()?.access_token ?? '';
  }
}
