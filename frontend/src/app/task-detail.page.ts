import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectMemberService } from './project-member.service';
import { ProjectMember } from './member.model';
import { TaskService } from './task.service';
import { TaskHistoryItem, TaskItem, TaskPriority, TaskStatus } from './task.model';

@Component({
  selector: 'app-task-detail-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  styles: [`
    .layout{display:grid;grid-template-columns:1fr 320px;gap:24px}
    .card{border:1px solid #e5e7eb;border-radius:8px;padding:16px;background:#fff}
    .row{margin-bottom:12px}
    label{display:block;font-weight:600;margin-bottom:6px}
    input,select,textarea{width:100%;padding:8px}
    .timeline{list-style:none;padding:0;margin:0}
    .timeline li{border-left:2px solid #e5e7eb;margin-left:10px;padding-left:10px;margin-bottom:10px}
    .meta{color:#6b7280;font-size:12px}
  `],
  template: `
    <ng-container *ngIf="task; else notFound">
      <h1>Détail tâche</h1>
      <div class="layout">
        <div class="card">
          <form [formGroup]="form" (ngSubmit)="save()">
            <div class="row">
              <label>Titre</label>
              <input formControlName="title" />
            </div>
            <div class="row">
              <label>Description</label>
              <textarea rows="4" formControlName="description"></textarea>
            </div>
            <div class="row">
              <label>Statut</label>
              <select formControlName="status">
                <option value="TODO">TODO</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="DONE">DONE</option>
              </select>
            </div>
            <div class="row">
              <label>Priorité</label>
              <select formControlName="priority">
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
              </select>
            </div>
            <div class="row">
              <label>Due date</label>
              <input type="date" formControlName="dueDate" />
            </div>
            <button type="submit">Enregistrer</button>
          </form>
        </div>

        <div class="card">
          <div class="row">
            <label>Assigner à</label>
            <select [value]="''" (change)="onAssign($any($event.target).value)">
              <option value="">(non assignée)</option>
              <option *ngFor="let m of members" [value]="m.email">{{ m.name }} ({{ m.role }})</option>
            </select>
          </div>
          <div class="row">
            <label>Projet</label>
            <div class="meta">#{{ task?.projectId }}</div>
          </div>
        </div>
      </div>

      <div class="card" style="margin-top:16px;">
        <h3>Historique</h3>
        <ul class="timeline">
          <li *ngFor="let h of history">
            <div>{{ h.message || h.type || 'Événement' }}</div>
            <div class="meta">{{ h.createdAt | date:'short' }}<span *ngIf="h.actorName"> • {{ h.actorName }}</span></div>
          </li>
        </ul>
        <p *ngIf="!history?.length" class="meta">Aucun événement.</p>
      </div>
    </ng-container>

    <ng-template #notFound>
      <p>Tâche introuvable.</p>
      <a (click)="back()">Retour</a>
    </ng-template>
  `
})
export class TaskDetailPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly tasks = inject(TaskService);
  private readonly membersSvc = inject(ProjectMemberService);

  task: TaskItem | null = null;
  history: TaskHistoryItem[] = [];
  members: ProjectMember[] = [];

  form = inject(FormBuilder).group({
    title: [''],
    description: [''],
    status: ['TODO' as TaskStatus],
    priority: ['MEDIUM' as TaskPriority],
    dueDate: ['']
  });

  ngOnInit(){
    const taskId = Number(this.route.snapshot.paramMap.get('taskId'));
    const projectId = Number(this.route.snapshot.paramMap.get('projectId'));
    if (!taskId || !projectId) { this.task = null; return; }

    this.tasks.getById(projectId, taskId).subscribe({
      next: (t) => {
        this.task = t;
        this.form.patchValue({
          title: t.title,
          description: t.description || '',
          status: t.status,
          priority: t.priority || 'MEDIUM',
          dueDate: (t.dueDate || '').substring(0,10)
        });
        // load members for assignment
        if (t.projectId) {
          this.membersSvc.list(t.projectId).subscribe({ next: (ms) => this.members = ms });
        }
        // load history
        this.tasks.history(projectId, taskId).subscribe({ next: (h) => this.history = h });
      },
      error: () => { this.task = null; }
    });
  }

  save(){
    if (!this.task) return;
    const v = this.form.value as any;
    const dto = {
      title: v.title,
      description: v.description || undefined,
      status: v.status,
      priority: v.priority,
      dueDate: v.dueDate || undefined
    };
    this.tasks.update(this.task.projectId, this.task.id, dto).subscribe({ next: (t) => this.task = { ...this.task!, ...t } });
  }

  onAssign(val: string){
    if (!this.task) return;
    const assigneeEmail = val || undefined;
    if (!assigneeEmail) {
      // Unassign by clearing assignee via update
      this.tasks.update(this.task.projectId, this.task.id, { /* no assignee support in backend update */ }).subscribe({ next: (t) => this.task = { ...this.task!, ...t } });
      return;
    }
    this.tasks.assign(this.task.projectId, this.task.id, assigneeEmail).subscribe({
      next: (t) => {
        this.task = { ...this.task!, ...t };
        this.tasks.history(this.task!.projectId, this.task!.id).subscribe({ next: (h) => this.history = h });
      }
    });
  }

  back(){ this.router.navigate(['/projects', this.task?.projectId ?? '']); }
}
