import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { HealthComponent } from './health.component';
import { ProjectsPageComponent } from './projects.page';
import { ProjectCreatePageComponent } from './project-create.page';
import { ProjectDetailPageComponent } from './project-detail.page';
import { ProjectMembersPageComponent } from './project-members.page';
import { TasksPageComponent } from './tasks.page';
import { ProjectTasksPageComponent } from './project-tasks.page';
import { BoardPageComponent } from './board.page';
import { ProjectBoardPageComponent } from './project-board.page';
import { NotificationsPageComponent } from './notifications.page';
import { TaskDetailPageComponent } from './task-detail.page';
import { RegisterPageComponent } from './register.page';
import { LoginPageComponent } from './login.page';
import { authGuard } from './auth.guard';
import { MyInvitationsPageComponent } from './my-invitations.page';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'health', component: HealthComponent },
  { path: 'projects', component: ProjectsPageComponent, canActivate: [authGuard] },
  { path: 'projects/new', component: ProjectCreatePageComponent, canActivate: [authGuard] },
  { path: 'projects/:projectId', component: ProjectDetailPageComponent, canActivate: [authGuard] },
  { path: 'projects/:projectId/members', component: ProjectMembersPageComponent, canActivate: [authGuard] },
  { path: 'projects/:projectId/tasks', component: ProjectTasksPageComponent, canActivate: [authGuard] },
  { path: 'projects/:projectId/board', component: ProjectBoardPageComponent, canActivate: [authGuard] },
  { path: 'tasks', component: TasksPageComponent, canActivate: [authGuard] },
  { path: 'projects/:projectId/tasks/:taskId', component: TaskDetailPageComponent, canActivate: [authGuard] },
  { path: 'board', component: BoardPageComponent, canActivate: [authGuard] },
  { path: 'notifications', component: NotificationsPageComponent, canActivate: [authGuard] },
  { path: 'invitations', component: MyInvitationsPageComponent, canActivate: [authGuard] },
  { path: 'register', component: RegisterPageComponent },
  { path: 'login', component: LoginPageComponent },
  { path: '**', redirectTo: '' }
];
