import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CreateProjectDto, Project } from './project.model';
import { SessionService } from './session.service';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  constructor(private http: HttpClient, private session: SessionService) {}

  list(query?: { q?: string; page?: number; size?: number; sort?: string }): Observable<Project[]> {
    let params = new HttpParams();
    if (query?.q) params = params.set('q', query.q);
    if (query?.page != null) params = params.set('page', query.page);
    if (query?.size != null) params = params.set('size', query.size);
    if (query?.sort) params = params.set('sort', query.sort);
    return this.http
      .get<any>(`/api/projects`, { params })
      .pipe(map(res => (Array.isArray(res) ? res : (res?.content ?? [])) as Project[]));
  }

  getById(projectId: number): Observable<Project> {
    return this.http.get<Project>(`/api/projects/${projectId}`);
    }

  create(payload: CreateProjectDto): Observable<Project> {
    // Backend expects { name, description?, startDate?, creatorEmail }
    const body: any = {
      name: payload.name,
      description: payload.description,
      creatorEmail: this.session.email
    };
    return this.http.post<Project>(`/api/projects`, body);
  }
}
