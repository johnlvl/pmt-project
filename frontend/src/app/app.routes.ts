import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { HealthComponent } from './health.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'health', component: HealthComponent },
  { path: '**', redirectTo: '' }
];
