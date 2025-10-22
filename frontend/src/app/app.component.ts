import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SpinnerComponent } from './spinner.component';
import { ErrorBannerComponent } from './error-banner.component';
import { NotificationService } from './notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, SpinnerComponent, ErrorBannerComponent],
  template: `
    <app-error-banner></app-error-banner>
    <header style="padding:8px;background:#0d6efd;color:#fff;display:flex;gap:12px;align-items:center">
      <a routerLink="/" style="color:#fff;text-decoration:none;font-weight:700;margin-right:8px">PMT</a>
      <nav style="display:flex;gap:12px">
        <a routerLink="/projects" routerLinkActive="active" style="color:#fff">Projets</a>
        <a routerLink="/tasks" routerLinkActive="active" style="color:#fff">Tâches</a>
        <a routerLink="/board" routerLinkActive="active" style="color:#fff">Board</a>
        <a routerLink="/notifications" routerLinkActive="active" style="color:#fff;position:relative;display:inline-flex;align-items:center;gap:6px">
          Notifications
          <span *ngIf="unreadCount>0" style="background:#dc3545;color:#fff;border-radius:12px;padding:2px 6px;font-size:12px;line-height:1">{{ unreadCount }}</span>
        </a>
        <a routerLink="/health" routerLinkActive="active" style="color:#fff">Health</a>
      </nav>
    </header>
    <main style="padding:16px">
      <router-outlet></router-outlet>
    </main>
    <app-spinner></app-spinner>
  `
})
export class AppComponent {
  config = window.RUNTIME_CONFIG || { API_BASE_URL: '/api' };
  private readonly notifications = inject(NotificationService);
  unreadCount = 0;

  ngOnInit(){
    this.notifications.unreadCount$.subscribe(c => this.unreadCount = c);
    this.notifications.refreshCount();
  }
}
