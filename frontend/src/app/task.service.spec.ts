import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TaskService } from './task.service';

describe('TaskService', () => {
  let svc: TaskService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    svc = TestBed.inject(TaskService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists tasks with projectId param', () => {
    svc.list(1).subscribe(items => expect(items).toEqual([]));
    const req = http.expectOne(r => r.method==='GET' && r.url.endsWith('/api/tasks') && r.params.get('projectId')==='1');
    req.flush([]);
  });

  it('creates task', () => {
    svc.create({ projectId: 1, title: 'T' }).subscribe(task => expect((task as any).title).toBe('T'));
    const req = http.expectOne(r => r.method==='POST' && r.url.endsWith('/api/tasks'));
    req.flush({ id: 5, projectId: 1, title: 'T', status: 'TODO' });
  });

  it('updates task', () => {
    svc.update(5, { status: 'DONE' as any }).subscribe(t => expect((t as any).status).toBe('DONE'));
    const req = http.expectOne(r => r.method==='PATCH' && r.url.endsWith('/api/tasks/5'));
    req.flush({ id: 5, projectId: 1, title: 'T', status: 'DONE' });
  });

  it('deletes task', () => {
    svc.remove(5).subscribe(() => expect(true).toBeTrue());
    const req = http.expectOne(r => r.method==='DELETE' && r.url.endsWith('/api/tasks/5'));
    req.flush({});
  });
});
