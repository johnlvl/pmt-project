import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProjectService } from './project.service';
import { Project } from './project.model';

@Component({
  selector: 'app-project-detail-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <ng-container *ngIf="project; else notFound">
      <h1>{{ project?.name }}</h1>
      <p>{{ project?.description || '—' }}</p>
      <div style="margin-top:12px; display:flex; gap:12px; align-items:center">
        <a [routerLink]="['/projects', project?.id, 'members']" class="btn">Gérer les membres</a>
        <a [routerLink]="['/projects', project?.id, 'tasks']" class="btn">Voir les tâches</a>
        <a [routerLink]="['/projects', project?.id, 'board']" class="btn">Tableau de bord</a>
      </div>
    </ng-container>
    <ng-template #notFound>
      <p>Projet introuvable.</p>
      <a (click)="back()">Retour à la liste</a>
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
