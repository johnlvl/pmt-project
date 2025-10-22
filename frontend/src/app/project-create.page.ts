import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ProjectService } from './project.service';

@Component({
  selector: 'app-project-create-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Nouveau projet</h1>
    <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
      <div>
        <label>Nom</label>
        <input formControlName="name" required />
        <div *ngIf="form.controls.name.touched && form.controls.name.invalid" style="color:#dc3545">Nom requis</div>
      </div>
      <div>
        <label>Description</label>
        <textarea formControlName="description" rows="3"></textarea>
      </div>
      <button type="submit" [disabled]="form.invalid">Créer</button>
    </form>
  `
})
export class ProjectCreatePageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProjectService);
  private readonly router = inject(Router);

  form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['']
  });

  submit(){
    if (this.form.invalid) return;
    this.service.create(this.form.value as any).subscribe({
      next: (p) => this.router.navigate(['/projects', (p as any).id])
    });
  }
}
