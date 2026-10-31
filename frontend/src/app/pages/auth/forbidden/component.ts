import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './component.html'
})
export class ForbiddenComponent {
  private auth = inject(AuthService);

  home(): string {
    return this.auth.isLoggedIn() ? this.auth.homeRoute() : '/login';
  }
}
