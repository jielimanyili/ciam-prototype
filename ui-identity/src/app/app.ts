import { Component, HostListener, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { ConfigService } from './config';

type Action = 'login' | 'logout' | 'jvm' | 'python' | 'admin';

interface MeResponse {
  claims: { sub?: string; scope?: string; [key: string]: unknown };
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  readonly auth = inject(AuthService);
  private http = inject(HttpClient);
  private config = inject(ConfigService);
  readonly apiResult = signal('');
  readonly apiError = signal('');
  /** The in-flight action, if any — drives the button spinner and disables the other buttons. */
  readonly pending = signal<Action | null>(null);

  // Sign in/out leave the page via redirect, so `pending` normally isn't reset; if the browser
  // restores this page from the back/forward cache, clear it so the buttons aren't stuck.
  @HostListener('window:pageshow', ['$event'])
  onPageShow(event: PageTransitionEvent): void {
    if (event.persisted) {
      this.pending.set(null);
    }
  }

  async signIn(): Promise<void> {
    this.pending.set('login');
    await this.auth.login();
    // Only reached if the redirect didn't happen (login() reports the error).
    this.pending.set(null);
  }

  async signOut(): Promise<void> {
    this.pending.set('logout');
    await this.auth.logout();
    this.pending.set(null);
  }

  callJvmMe(): void {
    void this.callApi('jvm', this.config.config().apiBaseUrl, '/api/me');
  }

  callPythonMe(): void {
    void this.callApi('python', this.config.config().pythonApiBaseUrl, '/api/me');
  }

  callAdmin(): void {
    // The user's token lacks the `admin` scope, so this demonstrates 403 scope enforcement.
    void this.callApi('admin', this.config.config().apiBaseUrl, '/api/admin');
  }

  private async callApi(action: Action, baseUrl: string, path: string): Promise<void> {
    const token = this.auth.accessToken();
    if (!token) {
      this.apiError.set('Not signed in');
      return;
    }
    this.apiError.set('');
    this.apiResult.set('');
    this.pending.set(action);
    try {
      const res = await firstValueFrom(
        this.http.get<MeResponse>(`${baseUrl}${path}`, {
          headers: new HttpHeaders({ Authorization: `Bearer ${token}` }),
        }),
      );
      this.apiResult.set(JSON.stringify(res, null, 2));
    } catch (e) {
      this.apiError.set((e as Error).message);
    } finally {
      this.pending.set(null);
    }
  }
}
