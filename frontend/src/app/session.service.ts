import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SessionService {
  // Returns a requester/user email for backend endpoints that require it
  get email(): string {
    const cfgEmail = (typeof window !== 'undefined' ? (window as any).RUNTIME_CONFIG?.USER_EMAIL : undefined);
    const lsEmail = (typeof window !== 'undefined') ? window.localStorage.getItem('userEmail') || undefined : undefined;
    return cfgEmail || lsEmail || 'alice@example.com';
  }

  set email(value: string) {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem('userEmail', value);
    }
  }
}
