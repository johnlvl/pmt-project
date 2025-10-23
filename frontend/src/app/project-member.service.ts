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

  invite(projectId: number, dto: InviteMemberDto): Observable<number> {
    // Backend expects { projectId, email } at /api/invitations and returns the invitation id (Integer)
    const body = { projectId, email: dto.email };
    return this.http.post<number>(`/api/invitations`, body);
  }

  changeRole(projectId: number, targetEmail: string, role: ProjectRole): Observable<void> {
    // Backend role change is POST /api/projects/assign-role with body { projectId, targetEmail, roleName }
    const body = { projectId, targetEmail, roleName: role } as any;
    return this.http.post<void>(`/api/projects/assign-role`, body);
  }

  remove(projectId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`/api/projects/${projectId}/members/${userId}`);
  }
}
