import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InviteMemberDto, ProjectMember, ProjectRole } from './member.model';

@Injectable({ providedIn: 'root' })
export class ProjectMemberService {
  constructor(private http: HttpClient) {}

  list(projectId: number): Observable<ProjectMember[]> {
    return this.http.get<ProjectMember[]>(`/api/projects/${projectId}/members`);
  }

  invite(projectId: number, dto: InviteMemberDto): Observable<void> {
    return this.http.post<void>(`/api/projects/${projectId}/invitations`, dto);
  }

  changeRole(projectId: number, userId: number, role: ProjectRole): Observable<void> {
    return this.http.patch<void>(`/api/projects/${projectId}/members/${userId}`, { role });
  }

  remove(projectId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`/api/projects/${projectId}/members/${userId}`);
  }
}
