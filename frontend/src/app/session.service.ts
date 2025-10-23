import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SessionService {
  // Returns a requester/user email for backend endpoints that require it
  get email(): string {
    const cfgEmail = (typeof window !== 'undefined' ? (window as any).RUNTIME_CONFIG?.USER_EMAIL : undefined);
    const lsEmail = (typeof window !== 'undefined') ? window.localStorage.getItem('userEmail') || undefined : undefined;
    // Do not return any default email when not logged in
    return cfgEmail || lsEmail || '';
  }

  set email(value: string) {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem('userEmail', value);
    }
  }

  // True if a user email is present in localStorage or runtime config
  get isLoggedIn(): boolean {
    if (typeof window === 'undefined') return false;
    const cfgEmail = (window as any).RUNTIME_CONFIG?.USER_EMAIL;
    const lsEmail = window.localStorage.getItem('userEmail');
    return !!(cfgEmail || lsEmail);
  }

  // Returns the current email if stored by the app (localStorage), otherwise null
  get currentEmailOrNull(): string | null {
    if (typeof window === 'undefined') return null;
    return window.localStorage.getItem('userEmail');
  }

  logout(): void {
    if (typeof window !== 'undefined') {
      window.localStorage.removeItem('userEmail');
    }
  }
}
