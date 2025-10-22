import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProjectService } from './project.service';
import { Project } from './project.model';

@Component({
  selector: 'app-projects-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  styles: [`
    table{border-collapse:collapse;width:100%}
    th,td{padding:8px;border-bottom:1px solid #e5e7eb;text-align:left}
    .toolbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}
  `],
  template: `
    <div class="toolbar">
      <h1>Projets</h1>
      <a routerLink="/projects/new">+ Nouveau projet</a>
    </div>
    <table *ngIf="projects?.length; else empty">
      <thead>
        <tr><th>Nom</th><th>Description</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let p of projects">
          <td><a [routerLink]="['/projects', p.id]">{{ p.name }}</a></td>
          <td>{{ p.description || '—' }}</td>
        </tr>
      </tbody>
    </table>
    <ng-template #empty>
      <p>Aucun projet pour le moment.</p>
    </ng-template>
  `
})
export class ProjectsPageComponent {
  private readonly service = inject(ProjectService);
  projects: Project[] = [];

  ngOnInit(){
    this.service.list().subscribe({ next: (items) => this.projects = items });
  }
}
