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
    .container{max-width:980px;margin:0 auto}
    .toolbar{display:flex;justify-content:space-between;align-items:center;margin:16px 0}
    .subtitle{color:#6b7280;margin:0}
    .card{border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;background:#fff}
    table{border-collapse:collapse;width:100%}
    th{background:#f9fafb;color:#374151;font-weight:600}
    th,td{padding:12px;border-bottom:1px solid #e5e7eb;text-align:left}
    tbody tr:hover{background:#f9fafb}
    .name a{color:#111827;text-decoration:none;font-weight:600}
    
  `],
  template: `
    <div class="container">
      <div class="toolbar">
        <div>
          <h1 style="margin:0">Projets</h1>
          <p class="subtitle">{{ projects?.length || 0 }} projet(s)</p>
        </div>
  <a routerLink="/projects/new" class="btn">+ Nouveau projet</a>
      </div>

      <div class="card" *ngIf="projects?.length; else empty">
        <table>
          <thead>
            <tr><th>Nom</th><th>Description</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let p of projects">
              <td class="name"><a [routerLink]="['/projects', p.id]">{{ p.name }}</a></td>
              <td>{{ p.description || '—' }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <ng-template #empty>
        <div class="card" style="text-align:center;color:#6b7280;padding:24px">
          <p style="margin-bottom:12px">Aucun projet pour le moment.</p>
          <a routerLink="/projects/new" class="btn">Créer un premier projet</a>
        </div>
      </ng-template>
    </div>
  `
})
export class ProjectsPageComponent {
  private readonly service = inject(ProjectService);
  projects: Project[] = [];

  ngOnInit(){
    this.service.list().subscribe({ next: (items) => this.projects = items });
  }
}
