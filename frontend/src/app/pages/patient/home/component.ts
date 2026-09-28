import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-patient-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './component.html'
})
export class PatientHomeComponent {
  private auth = inject(AuthService);

  get session() {
    return this.auth.session();
  }
}
