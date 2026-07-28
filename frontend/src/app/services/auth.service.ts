import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Credentials, LoginResponse } from '../models/auth.model';

// Prefer an HttpOnly, Secure, SameSite=strict cookie issued by the backend
// so the token is never reachable from JavaScript. If client storage is
// unavoidable, at minimum keep the token in memory (a signal) and avoid
// persisting it to localStorage.
const TOKEN_KEY = 'auth_token';
const USERNAME_KEY = 'auth_username';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly tokenSignal = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly usernameSignal = signal<string | null>(localStorage.getItem(USERNAME_KEY));

  readonly isLoggedIn = computed(() => !!this.tokenSignal());
  readonly username = this.usernameSignal.asReadonly();

  constructor(private readonly http: HttpClient) {}

  get token(): string | null {
    return this.tokenSignal();
  }

  register(credentials: Credentials): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/register`, credentials);
  }

  login(credentials: Credentials): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USERNAME_KEY, credentials.username);
        this.tokenSignal.set(response.token);
        this.usernameSignal.set(credentials.username);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    this.tokenSignal.set(null);
    this.usernameSignal.set(null);
  }
}
