import { Component, inject } from '@angular/core';
import { NgIf, AsyncPipe } from '@angular/common';
import { ErrorService } from './error.service';

@Component({
  selector: 'app-error-banner',
  standalone: true,
  imports: [NgIf, AsyncPipe],
  styles: [`
    .banner{position:sticky;top:0;z-index:1001;background:#dc3545;color:#fff;padding:8px 12px;display:flex;justify-content:space-between;align-items:center}
    .banner button{background:transparent;border:1px solid #fff;color:#fff;padding:2px 8px;border-radius:4px;cursor:pointer}
  `],
  template: `
    <div class="banner" *ngIf="message$ | async as msg">
      <span>{{ msg }}</span>
      <button type="button" (click)="clear()" aria-label="Fermer">×</button>
    </div>
  `
})
export class ErrorBannerComponent {
  private readonly svc = inject(ErrorService);
  readonly message$ = this.svc.error$;
  clear(){ this.svc.clear(); }
}
