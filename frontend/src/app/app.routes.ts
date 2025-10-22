import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { HealthComponent } from './health.component';
import { ProjectsPageComponent } from './projects.page';
import { TasksPageComponent } from './tasks.page';
import { BoardPageComponent } from './board.page';
import { NotificationsPageComponent } from './notifications.page';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'health', component: HealthComponent },
  { path: 'projects', component: ProjectsPageComponent },
  { path: 'tasks', component: TasksPageComponent },
  { path: 'board', component: BoardPageComponent },
  { path: 'notifications', component: NotificationsPageComponent },
  { path: '**', redirectTo: '' }
];
