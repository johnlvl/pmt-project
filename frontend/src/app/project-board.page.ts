import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskItem, TaskStatus } from './task.model';
import { TaskService } from './task.service';

@Component({
  selector: 'app-project-board-page',
  standalone: true,
  imports: [CommonModule],
  styles: [`
    .board { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
    .lane { background: #f7f7f9; border: 1px solid #e5e7eb; border-radius: 6px; min-height: 300px; padding: 8px; }
    .lane h2 { margin: 0 0 8px; font-size: 16px; }
  .card { background: white; border: 1px solid #e5e7eb; border-radius: 6px; padding: 8px; margin-bottom: 8px; cursor: grab; }
    .card .meta { color: #6b7280; font-size: 12px; margin-top: 4px; }
    .card .meta.overdue { color: #b91c1c; }
    .pill { display:inline-block; padding:2px 6px; border-radius:10px; font-size:11px; margin-right:6px; }
    .pill.low { background:#ecfdf5; color:#065f46; border:1px solid #10b981; }
    .pill.medium { background:#eff6ff; color:#1d4ed8; border:1px solid #3b82f6; }
    .pill.high { background:#fff7ed; color:#9a3412; border:1px solid #f97316; }
    .lane.drag-over { outline: 2px dashed #3b82f6; }
    .empty { color: #6b7280; font-style: italic; }
  `],
  template: `
    <h1>Board</h1>
    <div class="board">
      <div class="lane" [class.drag-over]="dragOver==='TODO'" (dragover)="onDragOver($event)" (drop)="onDrop($event, 'TODO')">
        <h2>TODO ({{lanes.TODO.length}})</h2>
        <ng-container *ngIf="lanes.TODO.length; else emptyTodo">
          <div class="card" *ngFor="let t of lanes.TODO" draggable="true" (dragstart)="onDragStart($event, t, 'TODO')" (dragend)="onDragEnd()" (click)="onCardClick(t)">
            {{ titleOf(t) }}
            <div class="meta" [class.overdue]="isOverdue(t)" *ngIf="t?.dueDate">Échéance: {{ t.dueDate | date:'yyyy-MM-dd' }}</div>
            <div class="meta">
              <span class="pill" [ngClass]="priorityClass(t.priority)">Priorité: {{ t.priority || 'MEDIUM' }}</span>
              <span>• Assigné: {{ t.assigneeEmail || '—' }}</span>
            </div>
          </div>
        </ng-container>
        <ng-template #emptyTodo><div class="empty">Aucune tâche</div></ng-template>
      </div>

      <div class="lane" [class.drag-over]="dragOver==='IN_PROGRESS'" (dragover)="onDragOver($event)" (drop)="onDrop($event, 'IN_PROGRESS')">
        <h2>IN PROGRESS ({{lanes.IN_PROGRESS.length}})</h2>
        <ng-container *ngIf="lanes.IN_PROGRESS.length; else emptyIn">
          <div class="card" *ngFor="let t of lanes.IN_PROGRESS" draggable="true" (dragstart)="onDragStart($event, t, 'IN_PROGRESS')" (dragend)="onDragEnd()" (click)="onCardClick(t)">
            {{ titleOf(t) }}
            <div class="meta" [class.overdue]="isOverdue(t)" *ngIf="t?.dueDate">Échéance: {{ t.dueDate | date:'yyyy-MM-dd' }}</div>
            <div class="meta">
              <span class="pill" [ngClass]="priorityClass(t.priority)">Priorité: {{ t.priority || 'MEDIUM' }}</span>
              <span>• Assigné: {{ t.assigneeEmail || '—' }}</span>
            </div>
          </div>
        </ng-container>
        <ng-template #emptyIn><div class="empty">Aucune tâche</div></ng-template>
      </div>

      <div class="lane" [class.drag-over]="dragOver==='DONE'" (dragover)="onDragOver($event)" (drop)="onDrop($event, 'DONE')">
        <h2>DONE ({{lanes.DONE.length}})</h2>
        <ng-container *ngIf="lanes.DONE.length; else emptyDone">
          <div class="card" *ngFor="let t of lanes.DONE" draggable="true" (dragstart)="onDragStart($event, t, 'DONE')" (dragend)="onDragEnd()" (click)="onCardClick(t)">
            {{ titleOf(t) }}
            <div class="meta" [class.overdue]="isOverdue(t)" *ngIf="t?.dueDate">Échéance: {{ t.dueDate | date:'yyyy-MM-dd' }}</div>
            <div class="meta">
              <span class="pill" [ngClass]="priorityClass(t.priority)">Priorité: {{ t.priority || 'MEDIUM' }}</span>
              <span>• Assigné: {{ t.assigneeEmail || '—' }}</span>
            </div>
          </div>
        </ng-container>
        <ng-template #emptyDone><div class="empty">Aucune tâche</div></ng-template>
      </div>
    </div>
  `
})
export class ProjectBoardPageComponent {
  private route = inject(ActivatedRoute);
  private svc = inject(TaskService);
  private router = inject(Router);

  projectId!: number;
  dragOver: TaskStatus | null = null;
  lanes: Record<TaskStatus, TaskItem[]> = { TODO: [], IN_PROGRESS: [], DONE: [] };
  private dragging = false;

  ngOnInit() {
    this.projectId = Number(this.route.snapshot.paramMap.get('projectId'));
    this.load();
  }

  load() {
    this.svc.board(this.projectId).subscribe({
      next: res => {
        const lanes = (res as any)?.lanes || {};
        // ensure items have title mapped for display
        this.lanes.TODO = (lanes['TODO'] || []).map((t: any) => ({ ...t, title: t.title ?? t.name }));
        this.lanes.IN_PROGRESS = (lanes['IN_PROGRESS'] || []).map((t: any) => ({ ...t, title: t.title ?? t.name }));
        this.lanes.DONE = (lanes['DONE'] || []).map((t: any) => ({ ...t, title: t.title ?? t.name }));
      }
    });
  }

  onDragStart(ev: DragEvent, task: TaskItem, from: TaskStatus) {
    ev.dataTransfer?.setData('text/plain', JSON.stringify({ id: task.id, from }));
    ev.dataTransfer?.setDragImage(new Image(), 0, 0);
    this.dragging = true;
  }
  onDragOver(ev: DragEvent) {
    ev.preventDefault();
  }
  onDragEnd() {
    this.dragging = false;
  }
  onDrop(ev: DragEvent, to: TaskStatus) {
    ev.preventDefault();
    const data = ev.dataTransfer?.getData('text/plain');
    if (!data) return;
    const parsed = JSON.parse(data) as { id: number; from: TaskStatus };
    if (parsed.from === to) return;

    // Optimistic move
    const source = this.lanes[parsed.from];
    const idx = source.findIndex(t => t.id === parsed.id);
    if (idx === -1) return;
    const [task] = source.splice(idx, 1);
    const dest = this.lanes[to];
    dest.unshift({ ...task, status: to });

    this.svc.updateStatus(this.projectId, parsed.id, to).subscribe({
      next: () => {},
      error: () => {
        // rollback on error
        const dIdx = dest.findIndex(t => t.id === parsed.id);
        if (dIdx !== -1) dest.splice(dIdx, 1);
        source.splice(idx, 0, task);
      }
    });
  }

  titleOf(t: any): string {
    return t?.title ?? t?.name ?? '';
  }

  isOverdue(t: any): boolean {
    if (!t?.dueDate) return false;
    try {
      const due = new Date(t.dueDate);
      const today = new Date();
      // Normalize to date-only comparison
      due.setHours(0,0,0,0);
      today.setHours(0,0,0,0);
      return due.getTime() < today.getTime() && t?.status !== 'DONE';
    } catch {
      return false;
    }
  }

  priorityClass(p: string | undefined | null): string {
    const v = (p || 'MEDIUM').toUpperCase();
    if (v === 'HIGH') return 'pill high';
    if (v === 'LOW') return 'pill low';
    return 'pill medium';
  }

  onCardClick(t: TaskItem) {
    if (this.dragging) { this.dragging = false; return; }
    this.router.navigate(['/projects', this.projectId, 'tasks', t.id]);
  }
}
