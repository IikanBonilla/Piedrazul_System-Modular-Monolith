import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './component.html'
})
export class NavbarComponent {
  menuOpen = false;
  private auth = inject(AuthService);
  private router = inject(Router);

  session() {
    return this.auth.session();
  }

  home(): string {
    const current = this.session();
    if (!current || current.expiresAt <= Date.now()) {
      return '/login';
    }
    if (current.role === 'PATIENT') {
      return '/paciente';
    }
    if (current.role === 'DOCTOR') {
      return '/medico';
    }
    return '/agendador';
  }

  roleLabel(): string {
    switch (this.session()?.role) {
      case 'ADMINISTRATOR':
        return 'Administrador';
      case 'DOCTOR':
        return 'Médico';
      case 'PATIENT':
        return 'Paciente';
      case 'SCHEDULER':
        return 'Agendador';
      default:
        return '';
    }
  }

  logout(): void {
    this.menuOpen = false;
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
