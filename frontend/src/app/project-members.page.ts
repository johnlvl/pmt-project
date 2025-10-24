import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProjectMemberService } from './project-member.service';
import { ProjectMember, ProjectRole } from './member.model';
import { RoleBadgeComponent } from './role-badge.component';

@Component({
  selector: 'app-project-members-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RoleBadgeComponent],
  styles: [`
    table{border-collapse:collapse;width:100%}
    th,td{padding:8px;border-bottom:1px solid #e5e7eb;text-align:left}
    .row-actions{display:flex;gap:8px}
    form{display:flex;gap:8px;align-items:flex-end;margin-bottom:12px}
    input,select{padding:6px}
    button{padding:6px 10px}
  `],
  template: `
    <h1>Membres du projet</h1>
    <div *ngIf="successMsg" style="margin:8px 0; padding:8px 12px; background:#ecfdf5; color:#065f46; border:1px solid #10b981; border-radius:6px">{{ successMsg }}</div>
    <div *ngIf="errorMsg" style="margin:8px 0; padding:8px 12px; background:#fef2f2; color:#991b1b; border:1px solid #ef4444; border-radius:6px">{{ errorMsg }}</div>
    <form [formGroup]="inviteForm" (ngSubmit)="invite()">
      <div>
        <label>Email</label><br />
        <input formControlName="email" type="email" required />
      </div>
      <div>
        <label>Rôle</label><br />
        <select formControlName="role">
          <option value="MEMBER">MEMBER</option>
          <option value="MAINTAINER">MAINTAINER</option>
          <option value="OWNER">OWNER</option>
        </select>
      </div>
      <button type="submit" [disabled]="inviteForm.invalid">Inviter</button>
    </form>

    <table *ngIf="members?.length; else empty">
      <thead>
        <tr><th>Nom</th><th>Email</th><th>Rôle</th><th>Actions</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let m of members">
          <td>{{ m.name }}</td>
          <td>{{ m.email }}</td>
          <td>
            <app-role-badge [role]="m.role"></app-role-badge>
            <select [value]="m.role" (change)="onChangeRole(m, $any($event.target).value)">
              <option value="MEMBER">Membre</option>
              <option value="MAINTAINER">Mainteneur</option>
              <option value="OWNER">Propriétaire</option>
            </select>
          </td>
          <td class="row-actions">
            <button (click)="remove(m)">Retirer</button>
          </td>
        </tr>
      </tbody>
    </table>
    <ng-template #empty>
      <p>Aucun membre pour le moment.</p>
    </ng-template>
  `
})
export class ProjectMembersPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly svc = inject(ProjectMemberService);
  private readonly fb = inject(FormBuilder);

  projectId!: number;
  members: ProjectMember[] = [];
  successMsg = '';
  errorMsg = '';

  inviteForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    role: ['MEMBER' as ProjectRole, Validators.required]
  });

  ngOnInit(){
    this.projectId = Number(this.route.snapshot.paramMap.get('projectId'));
    this.reload();
  }

  reload(){
    this.svc.list(this.projectId).subscribe({ next: (ms) => this.members = ms });
  }

  invite(){
    if (this.inviteForm.invalid) return;
    this.successMsg = '';
    this.errorMsg = '';
    const email = (this.inviteForm.value.email || '').toString();
    this.svc.invite(this.projectId, this.inviteForm.value as any).subscribe({
      next: (invitationId) => {
        this.inviteForm.reset({ email:'', role:'Membre' });
        this.reload();
        this.successMsg = `Invitation envoyée à ${email} (id ${invitationId}).`;
      },
      error: (err) => {
        this.errorMsg = err?.error?.message || 'Échec de l\'invitation. Vérifiez l\'email et réessayez.';
      }
    });
  }

  onChangeRole(m: ProjectMember, role: ProjectRole){
    if (role === m.role) return;
    this.svc.changeRole(this.projectId, m.email, role).subscribe({ next: () => m.role = role });
  }

  remove(m: ProjectMember){
    if (!confirm(`Retirer ${m.name} (${m.email}) du projet ?`)) return;
    this.svc.remove(this.projectId, m.userId).subscribe({ next: () => this.reload() });
  }
}
