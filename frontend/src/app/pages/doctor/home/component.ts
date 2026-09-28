import { Component, inject } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-doctor-home',
  standalone: true,
  templateUrl: './component.html'
})
export class DoctorHomeComponent {
  private auth = inject(AuthService);

  get session() {
    return this.auth.session();
  }
}
