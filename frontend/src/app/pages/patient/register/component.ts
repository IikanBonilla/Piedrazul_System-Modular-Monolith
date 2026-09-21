import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { PatientService } from '../../../core/services/patient/service';

@Component({
  selector: 'app-patient-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './component.html'
})
export class PatientRegisterComponent {
  username = '';
  password = '';
  fullName = '';
  documentNumber = '';
  phone = '';
  loading = false;
  formError = '';
  successMessage = '';

  constructor(private patientService: PatientService) {}

  register() {
    this.formError = '';
    this.successMessage = '';

    const missing = this.missingFields();
    if (missing.length > 0) {
      this.formError = `Debe completar los campos obligatorios: ${missing.join(', ')}.`;
      return;
    }

    this.loading = true;
    this.patientService
      .register({
        username: this.username.trim(),
        password: this.password,
        fullName: this.fullName.trim(),
        documentNumber: this.documentNumber.trim(),
        phone: this.phone.trim()
      })
      .pipe(finalize(() => { this.loading = false; }))
      .subscribe({
        next: (response) => {
          this.successMessage = response.message;
          sessionStorage.setItem('patientDocument', response.documentNumber);
        },
        error: (err) => {
          this.formError = err.error?.message || 'No se pudo completar el registro.';
        }
      });
  }

  private missingFields(): string[] {
    const missing: string[] = [];
    if (!this.username.trim()) missing.push('nombre de usuario');
    if (!this.password.trim()) missing.push('contrasena');
    if (!this.fullName.trim()) missing.push('nombre completo');
    if (!this.documentNumber.trim()) missing.push('documento');
    if (!this.phone.trim()) missing.push('telefono');
    return missing;
  }
}
