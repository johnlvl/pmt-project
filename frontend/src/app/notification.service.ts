import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { NotificationItem } from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  list(): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(`/api/notifications`).pipe(
      tap(list => this.updateUnreadCountFrom(list))
    );
  }

  markRead(id: number): Observable<void> {
    return this.http.post<void>(`/api/notifications/${id}/read`, {}).pipe(
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
}
