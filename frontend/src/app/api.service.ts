import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  get baseUrl(): string {
    const cfg = isPlatformBrowser(this.platformId) && typeof window !== 'undefined'
      ? window.RUNTIME_CONFIG?.API_BASE_URL
      : undefined;
    // Trim trailing slashes to avoid // when concatenating
    return (cfg ?? '/api').replace(/\/+$/, '');
  }

  health() {
    return this.http.get(`${this.baseUrl}/actuator/health`);
  }
}
