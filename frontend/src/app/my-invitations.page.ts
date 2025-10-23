import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InvitationService, InvitationListItem } from './invitation.service';

@Component({
  selector: 'app-my-invitations-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  styles: [`
    table{border-collapse:collapse;width:100%}
    th,td{padding:8px;border-bottom:1px solid #e5e7eb;text-align:left}
    .row-actions{display:flex;gap:8px}
    button{padding:6px 10px}
    .muted{color:#6b7280}
  `],
  template: `
    <h1>Mes invitations</h1>
    <p class="muted">Gérez ici les invitations à rejoindre des projets.</p>

    <div *ngIf="successMsg" style="margin:8px 0; padding:8px 12px; background:#ecfdf5; color:#065f46; border:1px solid #10b981; border-radius:6px">{{ successMsg }}</div>
    <div *ngIf="errorMsg" style="margin:8px 0; padding:8px 12px; background:#fef2f2; color:#991b1b; border:1px solid #ef4444; border-radius:6px">{{ errorMsg }}</div>

    <table *ngIf="invitations?.length; else empty">
      <thead>
        <tr><th>Projet</th><th>Reçu le</th><th>Statut</th><th style="width:1%">Actions</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let inv of invitations">
          <td>
            <a [routerLink]="['/projects', inv.projectId]">{{ inv.projectName }}</a>
          </td>
          <td>{{ inv.createdAt | date:'mediumDate' }}</td>
          <td>{{ inv.status }}</td>
          <td class="row-actions">
            <button (click)="accept(inv)" [disabled]="loadingIds.has(inv.id)">Accepter</button>
            <button (click)="decline(inv)" [disabled]="loadingIds.has(inv.id)" style="background:#ef4444;color:#fff;border:none;border-radius:4px">Refuser</button>
          </td>
        </tr>
      </tbody>
    </table>
    <ng-template #empty>
      <p>Aucune invitation en attente.</p>
    </ng-template>
  `
})
export class MyInvitationsPageComponent {
  private readonly svc = inject(InvitationService);
  invitations: InvitationListItem[] = [];
  loadingIds = new Set<number>();
  successMsg = '';
  errorMsg = '';

  ngOnInit(){
    this.reload();
  }

  reload(){
    this.svc.listMine('PENDING').subscribe({
      next: (list) => { this.invitations = list; },
      error: (err) => { this.errorMsg = err?.error?.message || 'Échec du chargement des invitations.'; }
    });
  }

  accept(inv: InvitationListItem){
    this.errorMsg = ''; this.successMsg = '';
    this.loadingIds.add(inv.id);
    this.svc.accept(inv.id).subscribe({
      next: () => { this.successMsg = `Invitation au projet "${inv.projectName}" acceptée.`; this.reload(); },
      error: (err) => { this.errorMsg = err?.error?.message || 'Échec de l\'acceptation.'; },
      complete: () => { this.loadingIds.delete(inv.id); }
    });
  }

  decline(inv: InvitationListItem){
    this.errorMsg = ''; this.successMsg = '';
    this.loadingIds.add(inv.id);
    this.svc.decline(inv.id).subscribe({
      next: () => { this.successMsg = `Invitation au projet "${inv.projectName}" refusée.`; this.reload(); },
      error: (err) => { this.errorMsg = err?.error?.message || 'Échec du refus.'; },
      complete: () => { this.loadingIds.delete(inv.id); }
    });
  }
}
