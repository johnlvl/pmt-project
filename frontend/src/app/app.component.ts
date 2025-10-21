import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';

declare global {
  interface Window { RUNTIME_CONFIG: { API_BASE_URL: string }; }
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <header style="padding:8px;background:#0d6efd;color:#fff;display:flex;gap:12px">
      <a routerLink="/" style="color:#fff;text-decoration:none;font-weight:600">PMT</a>
      <a routerLink="/health" style="color:#fff">Health</a>
    </header>
    <main style="padding:16px">
      <router-outlet></router-outlet>
    </main>
  `
})
export class AppComponent {
  http = inject(HttpClient);
  config = window.RUNTIME_CONFIG || { API_BASE_URL: '/api' };
}
