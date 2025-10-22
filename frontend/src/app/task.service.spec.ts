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

  it('loads board lanes', () => {
    svc.board(1).subscribe(res => expect((res as any).lanes).toBeDefined());
    const req = http.expectOne(r => r.method==='GET' && r.url.endsWith('/api/tasks/board') && r.params.get('projectId')==='1');
    req.flush({ lanes: { TODO: [], IN_PROGRESS: [], DONE: [] } });
  });

  it('updates status via /api/tasks/update', () => {
    svc.updateStatus(1, 42, 'DONE' as any).subscribe(t => expect((t as any).status).toBe('DONE'));
    const req = http.expectOne(r => r.method==='PATCH' && r.url.endsWith('/api/tasks/update'));
    expect(req.request.body).toEqual(jasmine.objectContaining({ projectId: 1, taskId: 42, status: 'DONE' }));
    req.flush({ id: 42, projectId: 1, title: 'X', status: 'DONE' });
  });

  it('gets task by id', () => {
    svc.getById(10).subscribe(t => expect((t as any).id).toBe(10));
    const req = http.expectOne(r => r.method==='GET' && r.url.endsWith('/api/tasks/10'));
    req.flush({ id: 10, projectId: 1, title: 'Detail', status: 'TODO' });
  });

  it('loads task history', () => {
    svc.history(10).subscribe(h => expect(h.length).toBe(2));
    const req = http.expectOne(r => r.method==='GET' && r.url.endsWith('/api/tasks/10/history'));
    req.flush([
      { id: 1, taskId: 10, type: 'CREATED', message: 'Créée', createdAt: new Date().toISOString() },
      { id: 2, taskId: 10, type: 'STATUS_CHANGED', message: 'TODO -> IN_PROGRESS', createdAt: new Date().toISOString() }
    ]);
  });

  it('assigns task', () => {
    svc.assign(1, 10, 7).subscribe(t => expect((t as any).assigneeId).toBe(7));
    const req = http.expectOne(r => r.method==='POST' && r.url.endsWith('/api/tasks/assign'));
    expect(req.request.body).toEqual(jasmine.objectContaining({ projectId: 1, taskId: 10, assigneeId: 7 }));
    req.flush({ id: 10, projectId: 1, title: 'Detail', status: 'TODO', assigneeId: 7 });
  });
});
