import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from './user.service';
import { SessionService } from './session.service';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  styles: [`
    .card{max-width:420px;margin:32px auto;border:1px solid #e5e7eb;border-radius:8px;padding:16px;background:#fff}
    .row{margin-bottom:12px}
    label{display:block;font-weight:600;margin-bottom:6px}
    input{width:100%;padding:8px}
    .error{color:#b91c1c;font-size:12px}
  `],
  template: `
    <div class="card">
      <h1>Inscription</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <div class="row">
          <label>Nom d'utilisateur</label>
          <input formControlName="username" placeholder="ex: alice" />
          <div class="error" *ngIf="form.controls.username.touched && form.controls.username.invalid">
            Le nom est requis (max 50 caractères).
          </div>
        </div>
        <div class="row">
          <label>Email</label>
          <input formControlName="email" placeholder="alice@example.com" />
          <div class="error" *ngIf="form.controls.email.touched && form.controls.email.invalid">
            Email invalide ou manquant.
          </div>
        </div>
        <div class="row">
          <label>Mot de passe</label>
          <input type="password" formControlName="password" placeholder="min. 6 caractères" />
          <div class="error" *ngIf="form.controls.password.touched && form.controls.password.invalid">
            Mot de passe requis (>= 6 caractères).
          </div>
        </div>
        <div class="row">
          <label>Confirmation du mot de passe</label>
          <input type="password" formControlName="confirmPassword" placeholder="Répétez le mot de passe" />
          <div class="error" *ngIf="(form.controls.confirmPassword.touched && form.controls.confirmPassword.invalid) || form.errors?.passwordMismatch">
            Les mots de passe doivent correspondre.
          </div>
        </div>

        <button type="submit" [disabled]="form.invalid || loading">Créer mon compte</button>
      </form>
      <p *ngIf="error" class="error" style="margin-top:12px;">{{ error }}</p>
    </div>
  `
})
export class RegisterPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly user = inject(UserService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  loading = false;
  error: string | null = null;

  form = this.fb.group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(255)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: [passwordsMatchValidator] });

  submit(){
    if (this.form.invalid || this.loading) return;
    this.loading = true;
    this.error = null;
    const dto = this.form.value as any;
    this.user.register(dto).subscribe({
      next: (res) => {
        // set session email and go to projects
        this.session.email = res.email;
        const redirect = this.route.snapshot.queryParamMap.get('redirect');
        const target = redirect && redirect.startsWith('/') && redirect !== '/login' && redirect !== '/register'
          ? redirect
          : '/projects';
        this.router.navigateByUrl(target);
      },
      error: (err) => {
        // Handle 409 for email already used and generic errors
        if (err?.status === 409) {
          this.error = 'Cette adresse e-mail est déjà utilisée.';
        } else if (err?.status === 400) {
          this.error = 'Données invalides. Merci de vérifier le formulaire.';
        } else {
          this.error = 'Une erreur est survenue. Veuillez réessayer.';
        }
        this.loading = false;
      },
      complete: () => { this.loading = false; }
    });
  }
}

function passwordsMatchValidator(ctrl: AbstractControl): ValidationErrors | null {
  const password = ctrl.get('password')?.value;
  const confirm = ctrl.get('confirmPassword')?.value;
  if (!password || !confirm) return null;
  return password === confirm ? null : { passwordMismatch: true };
}
