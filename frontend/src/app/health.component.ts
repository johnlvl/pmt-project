import { Component, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-health',
  standalone: true,
  template: `
    <h2>Health check</h2>
    <button (click)="check()">Ping backend</button>
    <pre>{{ result }}</pre>
  `
})
export class HealthComponent {
  http = inject(HttpClient);
  result = 'No call yet';
  check() {
    // Use interceptor-prefixed base URL by calling relative '/api' path
    this.http.get(`/api/actuator/health`).subscribe({
      next: (res) => this.result = JSON.stringify(res, null, 2),
      error: (err) => this.result = `Error: ${err?.status} ${err?.statusText}`
    });
  }
}
