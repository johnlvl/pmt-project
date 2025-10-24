import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-role-badge',
  standalone: true,
  template: `
    <span [style]="styleFor(role)">{{ role }}</span>
  `
})
export class RoleBadgeComponent {
  @Input() role: 'OWNER'|'MAINTAINER'|'MEMBER' = 'MEMBER';

  styleFor(role: string) {
    const base = 'padding:2px 6px;border-radius:6px;font-size:12px;';
    switch(role){
      case 'OWNER': return base + 'background:#6f42c1;color:#fff';
      case 'MAINTAINER': return base + 'background:#0d6efd;color:#fff';
      default: return base + 'background:#6c757d;color:#fff';
    }
  }
}
