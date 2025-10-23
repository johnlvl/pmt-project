import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { CreateTaskDto, TaskHistoryItem, TaskItem, TaskPriority, TaskStatus, UpdateTaskDto } from './task.model';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class TaskService {
  constructor(private http: HttpClient, private session: SessionService) {}

  list(projectId: number, query?: Partial<{ q: string; status: TaskStatus; assigneeEmail: string; priority: TaskPriority; page: number; size: number }>): Observable<TaskItem[]> {
    // Backend expects projectId and requesterEmail, optional assigneeEmail; returns TaskListItem[]
    let params = new HttpParams().set('projectId', projectId).set('requesterEmail', this.session.email);
    if (query?.assigneeEmail) params = params.set('assigneeEmail', query.assigneeEmail);
    return this.http.get<any[]>(`/api/tasks`, { params }).pipe(
      map(items => (items || []).map(it => this.mapListItemToTaskItem(it, projectId)))
    );
  }

  create(dto: CreateTaskDto): Observable<TaskItem> {
    // Backend expects TaskCreateRequest: { projectId, requesterEmail, name, description?, dueDate?, priority? }
    const body: any = {
      projectId: dto.projectId,
      requesterEmail: this.session.email,
      name: dto.title,
      description: dto.description,
      dueDate: dto.dueDate,
      priority: dto.priority
    };
    return this.http.post<any>(`/api/tasks`, body).pipe(map(res => this.mapTaskResponseToTaskItem(res)));
  }

  update(projectId: number, taskId: number, dto: UpdateTaskDto): Observable<TaskItem> {
    // Backend expects TaskUpdateRequest to /api/tasks/update
    const body: any = {
      taskId,
      projectId,
      requesterEmail: this.session.email,
      // map front fields to backend names
      name: dto.title,
      description: dto.description,
      status: dto.status,
      priority: dto.priority,
      dueDate: dto.dueDate
    };
    return this.http.patch<any>(`/api/tasks/update`, body).pipe(map(res => this.mapTaskResponseToTaskItem(res)));
  }

  remove(projectId: number, taskId: number): Observable<void> {
    const params = new HttpParams().set('projectId', projectId).set('requesterEmail', this.session.email);
    return this.http.delete<void>(`/api/tasks/${taskId}`, { params });
  }

  // Board (Kanban)
  board(projectId: number): Observable<{ lanes: Record<string, TaskItem[]> } | any> {
    const params = new HttpParams().set('projectId', projectId).set('requesterEmail', this.session.email);
    return this.http.get<{ lanes: Record<string, TaskItem[]> }>(`/api/tasks/board`, { params });
  }

  updateStatus(projectId: number, taskId: number, status: TaskStatus, requesterEmail?: string): Observable<TaskItem> {
    const body: any = { taskId, projectId, status, requesterEmail: requesterEmail || this.session.email };
    return this.http.patch<any>(`/api/tasks/update`, body).pipe(map(res => this.mapTaskResponseToTaskItem(res)));
  }

  // US6 — Task detail
  getById(projectId: number, taskId: number): Observable<TaskItem> {
    const params = new HttpParams().set('projectId', projectId).set('requesterEmail', this.session.email);
    return this.http.get<any>(`/api/tasks/${taskId}`, { params }).pipe(map(res => this.mapTaskResponseToTaskItem(res)));
  }

  history(projectId: number, taskId: number): Observable<TaskHistoryItem[]> {
    const params = new HttpParams().set('projectId', projectId).set('requesterEmail', this.session.email);
    return this.http.get<TaskHistoryItem[]>(`/api/tasks/${taskId}/history`, { params });
  }

  assign(projectId: number, taskId: number, assigneeEmail: string, requesterEmail?: string): Observable<TaskItem> {
    const body: any = { projectId, taskId, assigneeEmail, requesterEmail: requesterEmail || this.session.email };
    return this.http.post<any>(`/api/tasks/assign`, body).pipe(map(res => this.mapTaskResponseToTaskItem(res)));
  }

  // Map a backend TaskResponse into frontend TaskItem
  private mapTaskResponseToTaskItem(res: any): TaskItem {
    if (!res) return res;
    return {
      id: res.id,
      projectId: res.projectId,
      title: res.name ?? res.title,
      description: res.description,
      status: res.status,
      priority: res.priority,
      dueDate: res.dueDate
    } as TaskItem;
  }

  // Map a backend TaskListItem into frontend TaskItem
  private mapListItemToTaskItem(it: any, projectId?: number): TaskItem {
    return {
      id: it.id,
      projectId: projectId ?? (it.projectId || 0),
      title: it.name ?? it.title,
      description: it.description,
      status: it.status,
      priority: it.priority,
      dueDate: it.dueDate,
      assigneeEmail: it.assigneeEmail
    } as TaskItem;
  }
}
