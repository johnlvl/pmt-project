import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterModule],
  template: `
    <h1>Bienvenue sur PMT</h1>
    <p>Front Angular prêt. Naviguez vers Health pour tester l'API.</p>
    <p>
      <a routerLink="/register">Créer un compte</a>
      &nbsp;|&nbsp;
      <a routerLink="/login">Se connecter</a>
    </p>
  `
})
export class HomeComponent {}
