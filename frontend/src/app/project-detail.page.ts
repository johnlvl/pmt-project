import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProjectService } from './project.service';
import { Project } from './project.model';

@Component({
  selector: 'app-project-detail-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  styles: [`
    .container{max-width:980px;margin:0 auto}
    .header{display:flex;justify-content:space-between;align-items:flex-start;gap:12px;margin:16px 0}
    .subtitle{color:#6b7280;margin:4px 0 0}
    .actions{display:flex;gap:10px;flex-wrap:wrap}
    .card{border:1px solid #e5e7eb;border-radius:8px;padding:16px;background:#fff}
    .muted{color:#6b7280}
  `],
  template: `
    <div class="container" *ngIf="project; else notFound">
      <div class="header">
        <div>
          <h1 style="margin:0">{{ project?.name }}</h1>
          <p class="subtitle">ID projet: #{{ project?.id }}</p>
        </div>
        <div class="actions">
          <a [routerLink]="['/projects', project?.id, 'members']" class="btn">Gérer les membres</a>
          <a [routerLink]="['/projects', project?.id, 'tasks']" class="btn btn-secondary">Voir les tâches</a>
          <a [routerLink]="['/projects', project?.id, 'board']" class="btn btn-secondary">Tableau de bord</a>
        </div>
      </div>

      <div class="card">
        <h3 style="margin-top:0">Description</h3>
        <p class="muted" *ngIf="!project?.description">Aucune description fournie.</p>
        <div *ngIf="project?.description">{{ project?.description }}</div>
      </div>

      <div style="margin-top:16px">
  <a (click)="back()" class="btn btn-secondary">Retour à la liste</a>
      </div>
    </div>
    <ng-template #notFound>
      <div class="container">
        <div class="card">
          <p>Projet introuvable.</p>
          <a (click)="back()" class="btn btn-secondary">Retour à la liste</a>
        </div>
      </div>
    </ng-template>
  `
})
export class ProjectDetailPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(ProjectService);
  project: Project | null = null;

  ngOnInit(){
    const id = Number(this.route.snapshot.paramMap.get('projectId'));
    if (!id) { this.project = null; return; }
    this.service.getById(id).subscribe({
      next: (p) => this.project = p,
      error: () => this.project = null
    });
  }

  back(){ this.router.navigate(['/projects']); }
}
