import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, catchError, map, switchMap, tap, throwError } from 'rxjs';
import { API_BASE_URL } from '../api/api-config';
import { KEYCLOAK_CLIENT_ID, keycloakTokenUrl } from './keycloak.config';

export type UserRole = 'ADMINISTRATOR' | 'SCHEDULER' | 'DOCTOR' | 'PATIENT';

export interface SessionUser {
  token: string;
  expiresAt: number;
  id: number;
  username: string;
  fullName: string;
  role: UserRole;
  doctorId: number | null;
  email: string | null;
  phone: string | null;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  username: string;
  email: string;
  password: string;
  phone: string;
  documentNumber: string;
  birthDate: string;
  role: 'PATIENT' | 'DOCTOR';
  specialty: string;
}

interface KeycloakTokenResponse {
  access_token: string;
  expires_in: number;
}

interface SessionProfile {
  expiresAt: number;
  id: number;
  username: string;
  fullName: string;
  role: UserRole;
  doctorId: number | null;
  email: string | null;
  phone: string | null;
}

const STORAGE_KEY = 'piedrazul.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly session = signal<SessionUser | null>(this.readSession());

  constructor(private http: HttpClient) {}

  login(payload: LoginPayload): Observable<SessionUser> {
    const body = new HttpParams()
      .set('grant_type', 'password')
      .set('client_id', KEYCLOAK_CLIENT_ID)
      .set('username', payload.username)
      .set('password', payload.password);

    return this.http.post<KeycloakTokenResponse>(keycloakTokenUrl(), body).pipe(
      catchError((error: unknown) => throwError(() => this.asLoginError(error))),
      switchMap((token) =>
        this.http.get<SessionProfile>(`${API_BASE_URL}/auth/me`, {
          headers: { Authorization: `Bearer ${token.access_token}` }
        }).pipe(
          map((profile) => this.toSession(token, profile))
        )
      ),
      tap((session) => this.persist(session))
    );
  }

  register(payload: RegisterPayload): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${API_BASE_URL}/auth/register`, payload);
  }

  logout(): void {
    sessionStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
  }

  isLoggedIn(): boolean {
    const current = this.session();
    if (!current) {
      return false;
    }
    if (current.expiresAt <= Date.now()) {
      this.logout();
      return false;
    }
    return true;
  }

  homeRoute(): string {
    switch (this.session()?.role) {
      case 'PATIENT':
        return '/paciente';
      case 'DOCTOR':
        return '/medico';
      case 'ADMINISTRATOR':
        return '/admin/configuracion';
      case 'SCHEDULER':
        return '/agendador';
      default:
        return '/login';
    }
  }

  token(): string | null {
    return this.isLoggedIn() ? this.session()?.token ?? null : null;
  }

  private toSession(token: KeycloakTokenResponse, profile: SessionProfile): SessionUser {
    const fromToken = Date.now() + (token.expires_in ?? 0) * 1000;
    return {
      token: token.access_token,
      expiresAt: profile.expiresAt > Date.now() ? profile.expiresAt : fromToken,
      id: profile.id,
      username: profile.username,
      fullName: profile.fullName,
      role: profile.role,
      doctorId: profile.doctorId,
      email: profile.email,
      phone: profile.phone
    };
  }

  private asLoginError(error: unknown): unknown {
    if (!(error instanceof HttpErrorResponse)) {
      return error;
    }
    const keycloakError = error.error?.error;
    if (error.status === 401 || keycloakError === 'invalid_grant') {
      return new HttpErrorResponse({
        status: 401,
        error: { code: 'INVALID_CREDENTIALS', message: 'Usuario o contraseña incorrectos.' },
        url: error.url ?? undefined
      });
    }
    if (error.status === 0) {
      return new HttpErrorResponse({
        status: 0,
        error: { message: 'No se pudo conectar con el inicio de sesión. Verifica que Keycloak esté en ejecución.' },
        url: error.url ?? undefined
      });
    }
    return error;
  }

  private persist(session: SessionUser): void {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.session.set(session);
  }

  private readSession(): SessionUser | null {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      const parsed = JSON.parse(raw) as SessionUser;
      if (!parsed?.token || !parsed.expiresAt || parsed.expiresAt <= Date.now()) {
        sessionStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return parsed;
    } catch {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}
