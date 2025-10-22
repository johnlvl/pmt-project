import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TaskService } from './task.service';
import { TaskItem, TaskPriority, TaskStatus } from './task.model';

@Component({
  selector: 'app-project-tasks-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  styles: [`
    .toolbar{display:flex;gap:8px;align-items:flex-end;margin-bottom:12px;flex-wrap:wrap}
    table{border-collapse:collapse;width:100%}
    th,td{padding:8px;border-bottom:1px solid #e5e7eb;text-align:left}
    input,select{padding:6px}
  `],
  template: `
    <h1>Tâches</h1>
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
      <div>
        <label>Priorité</label><br />
        <select formControlName="priority">
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
      </div>
      <button type="submit">Créer</button>
    </form>

    <table *ngIf="items?.length; else empty">
      <thead>
        <tr><th>Titre</th><th>Statut</th><th>Priorité</th><th>Actions</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let t of items">
          <td>{{ t.title }}</td>
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

  projectId!: number;
  items: TaskItem[] = [];

  filters = inject(FormBuilder).group({
    q: [''],
    status: [''],
    priority: ['']
  });

  creator = inject(FormBuilder).group({
    title: [''],
    priority: ['MEDIUM' as TaskPriority]
  });

  ngOnInit(){
    this.projectId = Number(this.route.snapshot.paramMap.get('projectId'));
    this.reload();
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
    this.svc.create({ projectId: this.projectId, title: v.title, priority: v.priority }).subscribe({
      next: () => { this.creator.reset({ title: '', priority: 'MEDIUM' }); this.reload(); }
    });
  }

  update(t: TaskItem, dto: Partial<TaskItem>){
    this.svc.update(t.id, dto as any).subscribe({ next: (res) => Object.assign(t, res) });
  }

  remove(t: TaskItem){
    if (!confirm(`Supprimer la tâche "${t.title}" ?`)) return;
    this.svc.remove(t.id).subscribe({ next: () => this.reload() });
  }
}
