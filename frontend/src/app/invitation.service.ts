import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionService } from './session.service';

export interface InvitationListItem {
  id: number;
  projectId: number;
  projectName: string;
  email: string;
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED';
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class InvitationService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);

  listMine(status: 'PENDING' | 'ACCEPTED' | 'DECLINED' = 'PENDING'): Observable<InvitationListItem[]> {
    const email = this.session.email;
    return this.http.get<InvitationListItem[]>(`/api/invitations`, { params: { email, status } as any });
  }

  accept(id: number): Observable<void> {
    const email = this.session.email;
    return this.http.post<void>(`/api/invitations/${id}/accept`, null, { params: { email } as any });
  }

  decline(id: number): Observable<void> {
    return this.http.post<void>(`/api/invitations/${id}/decline`, null);
  }
}
