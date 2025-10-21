import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

declare global {
  interface Window { RUNTIME_CONFIG?: { API_BASE_URL?: string } }
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}
  get baseUrl() {
    return window.RUNTIME_CONFIG?.API_BASE_URL || '/api';
  }
  health() {
    return this.http.get(`${this.baseUrl}/actuator/health`);
  }
}
