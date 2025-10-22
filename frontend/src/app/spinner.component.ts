import { Component, inject } from '@angular/core';
import { NgIf, AsyncPipe } from '@angular/common';
import { LoadingService } from './loading.service';

@Component({
  selector: 'app-spinner',
  standalone: true,
  imports: [NgIf, AsyncPipe],
  styles: [`
    .overlay{position:fixed;inset:0;background:rgba(0,0,0,.15);display:flex;align-items:center;justify-content:center;z-index:1000}
    .spinner{width:40px;height:40px;border:4px solid #fff;border-top-color:#0d6efd;border-radius:50%;animation:spin 1s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
  `],
  template: `
    <div class="overlay" *ngIf="loading$ | async">
      <div class="spinner" aria-label="Loading"></div>
    </div>
  `
})
export class SpinnerComponent {
  private readonly svc = inject(LoadingService);
  readonly loading$ = this.svc.loading$;
}
