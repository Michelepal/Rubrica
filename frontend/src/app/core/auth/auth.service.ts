import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  username: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenKey = 'rubricaJavaAngular.token';

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, request).pipe(
      tap(response => this.storeToken(response.token))
    );
  }

  logout(): void {
    try {
      localStorage.removeItem(this.tokenKey);
    } catch (error) {
      console.error('Errore durante la rimozione del token di autenticazione.', error);
    }
  }

  token(): string | null {
    try {
      return localStorage.getItem(this.tokenKey);
    } catch (error) {
      console.error('Errore durante la lettura del token di autenticazione.', error);
      return null;
    }
  }

  isAuthenticated(): boolean {
    return Boolean(this.token());
  }

  private storeToken(token: string): void {
    try {
      localStorage.setItem(this.tokenKey, token);
    } catch (error) {
      console.error('Errore durante il salvataggio del token di autenticazione.', error);
      throw error;
    }
  }
}
