import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';

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
  readonly apiResult = signal('');
  readonly apiError = signal('');

  async callApi(): Promise<void> {
    const token = this.auth.accessToken();
    if (!token) {
      this.apiError.set('Not signed in');
      return;
    }
    this.apiError.set('');
    this.apiResult.set('');
    try {
      const res = await firstValueFrom(
        this.http.get<MeResponse>('http://localhost:4101/api/me', {
          headers: new HttpHeaders({ Authorization: `Bearer ${token}` }),
        }),
      );
      this.apiResult.set(JSON.stringify(res, null, 2));
    } catch (e) {
      this.apiError.set((e as Error).message);
    }
  }
}
