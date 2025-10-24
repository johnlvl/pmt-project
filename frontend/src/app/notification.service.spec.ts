import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let svc: NotificationService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    svc = TestBed.inject(NotificationService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists notifications and updates unread count', () => {
    let count = -1;
    svc.unreadCount$.subscribe(c => count = c);
    svc.list().subscribe(list => expect(list.length).toBe(2));
    const req = http.expectOne(r => r.method==='GET' && r.url.endsWith('/api/notifications'));
    req.flush([
      { id: 1, message: 'A', createdAt: new Date().toISOString(), read: false },
      { id: 2, message: 'B', createdAt: new Date().toISOString(), read: true }
    ]);
    expect(count).toBe(1);
  });

  it('marks as read and decrements unread count', () => {
    // seed count
    svc['unreadCountSubject'].next(3 as any);
    svc.markRead(1).subscribe(() => {});
    const req = http.expectOne(r => r.method==='POST' && r.url.endsWith('/api/notifications/1/read'));
    req.flush({});

    svc.unreadCount$.subscribe(c => expect(c).toBe(2));
  });
});
