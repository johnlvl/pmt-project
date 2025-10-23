import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from './notification.service';
import { NotificationItem } from './notification.model';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [CommonModule],
  styles: [`
    ul{list-style:none;padding:0}
    li{border-bottom:1px solid #e5e7eb;padding:10px 0;display:flex;justify-content:space-between;gap:12px}
    .meta{color:#6b7280;font-size:12px}
    .unread{font-weight:600}
  `],
  template: `
    <h1>Notifications</h1>
    <ul>
      <li *ngFor="let n of items" [class.unread]="!n.read">
        <div>
          <div>{{ n.message || n.title || 'Notification' }}</div>
          <div class="meta">{{ n.createdAt | date:'short' }}</div>
        </div>
        <div>
          <button *ngIf="!n.read" class="btn" (click)="markAsRead(n)">Marquer comme lue</button>
        </div>
      </li>
    </ul>
    <p *ngIf="!items.length" class="meta">Aucune notification.</p>
  `
})
export class NotificationsPageComponent {
  private readonly svc = inject(NotificationService);
  items: NotificationItem[] = [];

  ngOnInit(){
    this.reload();
  }

  reload(){
    this.svc.list().subscribe({ next: (list) => this.items = list });
  }

  markAsRead(n: NotificationItem){
    this.svc.markRead(n.id).subscribe({ next: () => { n.read = true; } });
  }
}
