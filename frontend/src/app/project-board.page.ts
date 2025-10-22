import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
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
    .lane.drag-over { outline: 2px dashed #3b82f6; }
    .empty { color: #6b7280; font-style: italic; }
  `],
  template: `
    <h1>Board</h1>
    <div class="board">
      <div class="lane" [class.drag-over]="dragOver==='TODO'" (dragover)="onDragOver($event)" (drop)="onDrop($event, 'TODO')">
        <h2>TODO ({{lanes.TODO.length}})</h2>
        <ng-container *ngIf="lanes.TODO.length; else emptyTodo">
          <div class="card" *ngFor="let t of lanes.TODO" draggable="true" (dragstart)="onDragStart($event, t, 'TODO')">
            {{ titleOf(t) }}
          </div>
        </ng-container>
        <ng-template #emptyTodo><div class="empty">Aucune tâche</div></ng-template>
      </div>

      <div class="lane" [class.drag-over]="dragOver==='IN_PROGRESS'" (dragover)="onDragOver($event)" (drop)="onDrop($event, 'IN_PROGRESS')">
        <h2>IN PROGRESS ({{lanes.IN_PROGRESS.length}})</h2>
        <ng-container *ngIf="lanes.IN_PROGRESS.length; else emptyIn">
          <div class="card" *ngFor="let t of lanes.IN_PROGRESS" draggable="true" (dragstart)="onDragStart($event, t, 'IN_PROGRESS')">
            {{ titleOf(t) }}
          </div>
        </ng-container>
        <ng-template #emptyIn><div class="empty">Aucune tâche</div></ng-template>
      </div>

      <div class="lane" [class.drag-over]="dragOver==='DONE'" (dragover)="onDragOver($event)" (drop)="onDrop($event, 'DONE')">
        <h2>DONE ({{lanes.DONE.length}})</h2>
        <ng-container *ngIf="lanes.DONE.length; else emptyDone">
          <div class="card" *ngFor="let t of lanes.DONE" draggable="true" (dragstart)="onDragStart($event, t, 'DONE')">
            {{ titleOf(t) }}
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

  projectId!: number;
  dragOver: TaskStatus | null = null;
  lanes: Record<TaskStatus, TaskItem[]> = { TODO: [], IN_PROGRESS: [], DONE: [] };

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
  }
  onDragOver(ev: DragEvent) {
    ev.preventDefault();
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
}
