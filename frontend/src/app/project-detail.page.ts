import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectService } from './project.service';
import { Project } from './project.model';

@Component({
  selector: 'app-project-detail-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <ng-container *ngIf="project; else notFound">
      <h1>{{ project?.name }}</h1>
      <p>{{ project?.description || '—' }}</p>
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
