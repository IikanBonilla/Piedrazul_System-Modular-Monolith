import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../api/api-config';

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

const STORAGE_KEY = 'piedrazul.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly session = signal<SessionUser | null>(this.readSession());

  constructor(private http: HttpClient) {}

  login(payload: LoginPayload): Observable<SessionUser> {
    return this.http.post<SessionUser>(`${API_BASE_URL}/auth/login`, payload).pipe(
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
