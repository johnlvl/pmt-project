import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProjectService } from './project.service';

describe('ProjectService', () => {
  let svc: ProjectService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
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
    svc.create(payload).subscribe(p => expect((p as any).id).toBe(2));
    const req = http.expectOne(r => r.method === 'POST' && r.url.endsWith('/api/projects'));
    req.flush(created);
  });

  it('gets by id', () => {
    const item = { id: 3, name: 'X' };
    svc.getById(3).subscribe(p => expect((p as any).name).toBe('X'));
    const req = http.expectOne(r => r.method === 'GET' && r.url.endsWith('/api/projects/3'));
    req.flush(item);
  });
});
