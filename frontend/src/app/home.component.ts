import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterModule],
  template: `
    <section style="min-height:60vh;display:flex;align-items:center;justify-content:center">
      <div style="text-align:center;max-width:720px;padding:24px">
        <h1 style="font-size:40px;margin-bottom:12px">Bienvenue sur PMT</h1>
        <p style="color:#6b7280;margin-bottom:24px">Planifiez, suivez et collaborez sur vos projets d'équipe.</p>
        <div style="display:flex;gap:12px;justify-content:center;flex-wrap:wrap">
          <a routerLink="/register" class="btn" style="padding:10px 16px">Créer un compte</a>
          <a routerLink="/login" class="btn btn-secondary" style="padding:10px 16px">Se connecter</a>
        </div>
      </div>
    </section>
  `
})
export class HomeComponent {}
