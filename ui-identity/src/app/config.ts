import { Injectable, signal } from '@angular/core';

/**
 * Runtime configuration loaded from `assets/config.json`. The static S3+CloudFront build
 * stays IdP/host-agnostic: the same bundle is pointed at Keycloak (local) or Okta (prod)
 * by swapping this file — no rebuild (Epic 4, story 1).
 */
export interface AppConfig {
  authority: string;
  apiBaseUrl: string;
}

export const DEFAULT_CONFIG: AppConfig = {
  authority: 'http://localhost:8080/realms/ciam-demo',
  apiBaseUrl: 'http://localhost:4101',
};

@Injectable({ providedIn: 'root' })
export class ConfigService {
  readonly config = signal<AppConfig>(DEFAULT_CONFIG);

  async load(): Promise<void> {
    try {
      const res = await fetch('assets/config.json', { cache: 'no-store' });
      if (!res.ok) return;
      const json = (await res.json()) as Partial<AppConfig>;
      this.config.set({ ...DEFAULT_CONFIG, ...json });
    } catch {
      // keep defaults (local Keycloak + local resource server)
    }
  }
}