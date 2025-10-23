import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TaskService } from './task.service';
import { TaskItem, TaskPriority, TaskStatus } from './task.model';
import { ProjectMemberService } from './project-member.service';
import { ProjectMember } from './member.model';

@Component({
  selector: 'app-project-tasks-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  styles: [`
    .toolbar{display:flex;gap:8px;align-items:flex-end;margin-bottom:12px;flex-wrap:wrap}
    table{border-collapse:collapse;width:100%}
    th,td{padding:8px;border-bottom:1px solid #e5e7eb;text-align:left}
    input,select{padding:6px}
  `],
  template: `
    <h1>Tâches</h1>
    <div style="margin-bottom:10px">
      <a [routerLink]="['/projects', projectId, 'board']" class="btn">Ouvrir le tableau de bord</a>
    </div>
    <form class="toolbar" [formGroup]="filters" (ngSubmit)="applyFilters()">
      <div>
        <label>Recherche</label><br />
        <input formControlName="q" placeholder="Titre/description" />
      </div>
      <div>
        <label>Statut</label><br />
        <select formControlName="status">
          <option value="">(tous)</option>
          <option value="TODO">TODO</option>
          <option value="IN_PROGRESS">IN_PROGRESS</option>
          <option value="DONE">DONE</option>
        </select>
      </div>
      <div>
        <label>Priorité</label><br />
        <select formControlName="priority">
          <option value="">(toutes)</option>
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
      </div>
      <button type="submit">Appliquer</button>
    </form>

    <form class="toolbar" [formGroup]="creator" (ngSubmit)="create()">
      <div>
        <label>Titre</label><br />
        <input formControlName="title" required />
      </div>
      <div style="min-width:240px">
        <label>Description</label><br />
        <input formControlName="description" placeholder="Description de la tâche" />
      </div>
      <div>
        <label>Échéance</label><br />
        <input type="date" formControlName="dueDate" />
      </div>
      <div>
        <label>Priorité</label><br />
        <select formControlName="priority">
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
      </div>
      <div>
        <label>Assigner à</label><br />
        <select formControlName="assigneeEmail">
          <option value="">(aucun)</option>
          <option *ngFor="let m of members" [value]="m.email">{{ m.name }} ({{ m.role }})</option>
        </select>
      </div>
      <button type="submit">Créer</button>
    </form>

    <table *ngIf="items?.length; else empty">
      <thead>
        <tr><th>Titre</th><th>Description</th><th>Échéance</th><th>Statut</th><th>Priorité</th><th>Assigner à</th><th>Actions</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let t of items">
          <td>
            <input [value]="t.title" (change)="update(t, { title: $any($event.target).value })" />
          </td>
          <td>
            <input [value]="t.description || ''" placeholder="(aucune)" (change)="update(t, { description: $any($event.target).value || undefined })" />
          </td>
          <td>
            <input type="date" [value]="t.dueDate || ''" (change)="update(t, { dueDate: $any($event.target).value || undefined })" />
          </td>
          <td>
            <select [value]="t.status" (change)="update(t, { status: $any($event.target).value })">
              <option value="TODO">TODO</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="DONE">DONE</option>
            </select>
          </td>
          <td>
            <select [value]="t.priority || 'MEDIUM'" (change)="update(t, { priority: $any($event.target).value })">
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
            </select>
          </td>
          <td>
            <select [value]="t.assigneeEmail || ''" (change)="assign(t, $any($event.target).value)">
              <option value="">(aucun)</option>
              <option *ngFor="let m of members" [value]="m.email">{{ m.name }}</option>
            </select>
          </td>
          <td>
            <button (click)="remove(t)">Supprimer</button>
          </td>
        </tr>
      </tbody>
    </table>
    <ng-template #empty><p>Aucune tâche pour le moment.</p></ng-template>
  `
})
export class ProjectTasksPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly svc = inject(TaskService);
  private readonly membersSvc = inject(ProjectMemberService);

  projectId!: number;
  items: TaskItem[] = [];
  members: ProjectMember[] = [];

  filters = inject(FormBuilder).group({
    q: [''],
    status: [''],
    priority: ['']
  });

  creator = inject(FormBuilder).group({
    title: [''],
    description: [''],
    dueDate: [''],
    priority: ['MEDIUM' as TaskPriority],
    assigneeEmail: ['']
  });

  ngOnInit(){
    this.projectId = Number(this.route.snapshot.paramMap.get('projectId'));
    this.reload();
    this.membersSvc.list(this.projectId).subscribe({ next: (ms) => this.members = ms });
  }

  reload(){
    const v = this.filters.value as any;
    this.svc.list(this.projectId, {
      q: v.q || undefined,
      status: v.status || undefined,
      priority: v.priority || undefined
    }).subscribe({ next: (items) => this.items = items });
  }

  applyFilters(){ this.reload(); }

  create(){
    const v = this.creator.value as any;
    if (!v.title) return;
    this.svc.create({ projectId: this.projectId, title: v.title, description: v.description || undefined, dueDate: v.dueDate || undefined, priority: v.priority }).subscribe({
      next: (created) => {
        const email = v.assigneeEmail as string;
        const resetForm = () => this.creator.reset({ title: '', description: '', dueDate: '', priority: 'MEDIUM', assigneeEmail: '' });
        if (email) {
          this.svc.assign(this.projectId, created.id, email).subscribe({ next: () => { resetForm(); this.reload(); } });
        } else {
          resetForm(); this.reload();
        }
      }
    });
  }

  update(t: TaskItem, dto: Partial<TaskItem>){
    this.svc.update(t.projectId, t.id, dto as any).subscribe({ next: (res) => Object.assign(t, res) });
  }

  remove(t: TaskItem){
    if (!confirm(`Supprimer la tâche "${t.title}" ?`)) return;
    this.svc.remove(t.projectId, t.id).subscribe({
      next: () => {
        this.items = this.items.filter(x => x.id !== t.id);
      }
    });
  }

  assign(t: TaskItem, email: string){
    if (!email) return; // pas d'unassign dans l'API actuelle
    this.svc.assign(t.projectId, t.id, email).subscribe({ next: () => { t.assigneeEmail = email; } });
  }
}
