import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, map, of } from 'rxjs';
import { NotificationItem } from './notification.model';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient, private session: SessionService) {}

  list(): Observable<NotificationItem[]> {
    // Avoid calling backend when no user is logged in
    if (!this.session.isLoggedIn || !this.session.email) {
      this.unreadCountSubject.next(0);
      return of([]);
    }
    const params: any = { userEmail: this.session.email };
    return this.http.get<any[]>(`/api/notifications`, { params }).pipe(
      // Normalize backend payload (isRead -> read, createdAt to ISO)
      map(list => (list || []).map(n => this.mapItem(n))),
      tap(list => this.updateUnreadCountFrom(list))
    );
  }

  markRead(id: number): Observable<void> {
    if (!this.session.isLoggedIn || !this.session.email) {
      // Nothing to do if not logged in
      return of(void 0);
    }
    const params: any = { userEmail: this.session.email };
    return this.http.post<void>(`/api/notifications/${id}/read`, {}, { params }).pipe(
      tap(() => this.unreadCountSubject.next(Math.max(0, this.unreadCountSubject.value - 1)))
    );
  }

  refreshCount(): void {
    this.list().subscribe({ next: () => {}, error: () => {} });
  }

  private updateUnreadCountFrom(list: NotificationItem[]){
    const count = list.filter(n => !n.read).length;
    this.unreadCountSubject.next(count);
  }

  private mapItem(n: any): NotificationItem {
    const createdAt = n.createdAt || n.changeDate;
    const read = typeof n.read === 'boolean' ? n.read : !!n.isRead;
    return {
      id: n.id,
      message: n.message ?? n.title,
      title: n.title,
      createdAt: createdAt ? new Date(createdAt).toISOString() : new Date().toISOString(),
      read
    } as NotificationItem;
  }
}
