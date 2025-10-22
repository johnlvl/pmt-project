import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateProjectDto, Project } from './project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  constructor(private http: HttpClient) {}

  list(query?: { q?: string; page?: number; size?: number; sort?: string }): Observable<Project[]> {
    let params = new HttpParams();
    if (query?.q) params = params.set('q', query.q);
    if (query?.page != null) params = params.set('page', query.page);
    if (query?.size != null) params = params.set('size', query.size);
    if (query?.sort) params = params.set('sort', query.sort);
    return this.http.get<Project[]>(`/api/projects`, { params });
  }

  getById(projectId: number): Observable<Project> {
    return this.http.get<Project>(`/api/projects/${projectId}`);
    }

  create(payload: CreateProjectDto): Observable<Project> {
    return this.http.post<Project>(`/api/projects`, payload);
  }
}
