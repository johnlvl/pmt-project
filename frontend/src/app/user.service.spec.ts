import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';

describe('UserService', () => {
  let svc: UserService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    svc = TestBed.inject(UserService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('registers a user', () => {
    const payload = { username: 'alice', email: 'alice@example.com', password: 'secret123' };
    svc.register(payload).subscribe(res => expect(res.email).toBe('alice@example.com'));
    const req = http.expectOne(r => r.method==='POST' && r.url.endsWith('/api/users/register'));
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 1, username: 'alice', email: 'alice@example.com' });
  });
});
