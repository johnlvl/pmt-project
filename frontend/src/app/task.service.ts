import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateTaskDto, TaskHistoryItem, TaskItem, TaskPriority, TaskStatus, UpdateTaskDto } from './task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  constructor(private http: HttpClient) {}

  list(projectId: number, query?: { q?: string; status?: TaskStatus; assigneeId?: number; priority?: TaskPriority; page?: number; size?: number }): Observable<TaskItem[]> {
    let params = new HttpParams().set('projectId', projectId);
    if (query?.q) params = params.set('q', query.q);
    if (query?.status) params = params.set('status', query.status);
    if (query?.assigneeId != null) params = params.set('assigneeId', query.assigneeId);
    if (query?.priority) params = params.set('priority', query.priority);
    if (query?.page != null) params = params.set('page', query.page);
    if (query?.size != null) params = params.set('size', query.size);
    return this.http.get<TaskItem[]>(`/api/tasks`, { params });
  }

  create(dto: CreateTaskDto): Observable<TaskItem> {
    return this.http.post<TaskItem>(`/api/tasks`, dto);
  }

  update(taskId: number, dto: UpdateTaskDto): Observable<TaskItem> {
    return this.http.patch<TaskItem>(`/api/tasks/${taskId}`, dto);
  }

  remove(taskId: number): Observable<void> {
    return this.http.delete<void>(`/api/tasks/${taskId}`);
  }

  // Board (Kanban)
  board(projectId: number): Observable<{ lanes: Record<string, TaskItem[]> } | any> {
    const params = new HttpParams().set('projectId', projectId);
    return this.http.get<{ lanes: Record<string, TaskItem[]> }>(`/api/tasks/board`, { params });
  }

  updateStatus(projectId: number, taskId: number, status: TaskStatus, requesterEmail?: string): Observable<TaskItem> {
    const body: any = { taskId, projectId, status };
    if (requesterEmail) body.requesterEmail = requesterEmail;
    return this.http.patch<TaskItem>(`/api/tasks/update`, body);
  }

  // US6 — Task detail
  getById(taskId: number): Observable<TaskItem> {
    return this.http.get<TaskItem>(`/api/tasks/${taskId}`);
  }

  history(taskId: number): Observable<TaskHistoryItem[]> {
    return this.http.get<TaskHistoryItem[]>(`/api/tasks/${taskId}/history`);
  }

  assign(projectId: number, taskId: number, assigneeId: number, requesterEmail?: string): Observable<TaskItem> {
    const body: any = { projectId, taskId, assigneeId };
    if (requesterEmail) body.requesterEmail = requesterEmail;
    return this.http.post<TaskItem>(`/api/tasks/assign`, body);
  }
}
