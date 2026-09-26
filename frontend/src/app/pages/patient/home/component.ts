import { Component, inject } from '@angular/core';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-patient-home',
  standalone: true,
  templateUrl: './component.html'
})
export class PatientHomeComponent {
  private auth = inject(AuthService);

  get session() {
    return this.auth.session();
  }
}
