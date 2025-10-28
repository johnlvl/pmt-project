import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SpinnerComponent } from './spinner.component';
import { NotificationService } from './notification.service';
import { SessionService } from './session.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, SpinnerComponent],
  template: `
    <header style="padding:8px;background:#0d6efd;color:#fff;display:flex;gap:12px;align-items:center">
      <a routerLink="/" class="nav-link" style="font-weight:700;margin-right:8px">PMT</a>
      <nav style="display:flex;gap:12px;flex:1">
        <a routerLink="/projects" routerLinkActive="active" class="nav-link">Projets</a>
        <a routerLink="/invitations" routerLinkActive="active" class="nav-link">Mes invitations</a>
        <a routerLink="/notifications" routerLinkActive="active" class="nav-link" style="position:relative;display:inline-flex;align-items:center;gap:6px">
          Notifications
          <span *ngIf="unreadCount>0" style="background:#dc3545;color:#fff;border-radius:12px;padding:2px 6px;font-size:12px;line-height:1">{{ unreadCount }}</span>
        </a>
        <a routerLink="/health" routerLinkActive="active" class="nav-link">Health</a>
      </nav>
      <div style="display:flex;align-items:center;gap:8px">
        <span *ngIf="currentUserEmail" style="opacity:.9">{{ currentUserEmail }}</span>
  <button *ngIf="showLogout" (click)="logout()" class="btn btn-danger" style="cursor:pointer">Se déconnecter</button>
        <a *ngIf="!showLogout" routerLink="/login" class="nav-link">Se connecter</a>
      </div>
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
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  unreadCount = 0;
  get showLogout() { return this.session.isLoggedIn; }
  get currentUserEmail() { return this.session.email; }

  ngOnInit(){
    this.notifications.unreadCount$.subscribe(c => this.unreadCount = c);
    if (this.session.isLoggedIn) {
      this.notifications.refreshCount();
    }
  }

  logout(){
    this.session.logout();
    this.router.navigate(['/login']);
  }
}
