import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProjectService } from './project.service';
import { SessionService } from './session.service';

describe('ProjectService', () => {
  let svc: ProjectService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: SessionService, useValue: { email: 'alice@example.com' } }]
    });
    svc = TestBed.inject(ProjectService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists projects', () => {
    const mock = [{ id: 1, name: 'P1' }];
    svc.list().subscribe(items => expect(items.length).toBe(1));
    const req = http.expectOne(r => r.method === 'GET' && r.url.endsWith('/api/projects'));
    req.flush(mock);
  });

  it('creates project', () => {
    const payload = { name: 'New' };
    const created = { id: 2, name: 'New' };
    svc.create(payload).subscribe(p => {
      expect((p as any).id).toBe(2);
    });
    const req = http.expectOne(r => r.method === 'POST' && r.url.endsWith('/api/projects'));
    // ensure creatorEmail from session is sent
    expect(req.request.body).toEqual(jasmine.objectContaining({ creatorEmail: 'alice@example.com' }));
    req.flush(created);
  });

  it('gets by id', () => {
    const item = { id: 3, name: 'X' };
    svc.getById(3).subscribe(p => expect((p as any).name).toBe('X'));
    const req = http.expectOne(r => r.method === 'GET' && r.url.endsWith('/api/projects/3'));
    req.flush(item);
  });

  it('list supports query params (q, page, size, sort)', () => {
    svc.list({ q: 'abc', page: 1, size: 10, sort: 'name,asc' }).subscribe(items => expect(items.length).toBe(0));
    const req = http.expectOne(r => r.method === 'GET' && r.url.endsWith('/api/projects'));
    expect(req.request.params.get('q')).toBe('abc');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('name,asc');
    req.flush([]);
  });

  it('list maps page response with content array', () => {
    const page = { content: [{ id: 7, name: 'P' }] } as any;
    svc.list().subscribe(items => {
      expect(Array.isArray(items)).toBeTrue();
      expect((items as any[]).length).toBe(1);
      expect((items as any[])[0].id).toBe(7);
    });
    const req = http.expectOne(r => r.method === 'GET' && r.url.endsWith('/api/projects'));
    req.flush(page);
  });
});
