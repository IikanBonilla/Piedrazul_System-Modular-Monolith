import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidatorFn } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { httpErrorMessage, httpFieldErrors } from '../../../core/http/error-message';
import {
  birthDateError,
  documentError,
  emailError,
  normalizeSpaces,
  passwordError,
  personNameError,
  phoneError,
  specialtyError,
  toIsoDate,
  usernameError
} from '../../../core/validation/validators';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './component.html'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  loading = false;
  showPassword = false;
  formError = '';
  readonly today = toIsoDate(new Date());

  readonly form = this.fb.nonNullable.group({
    fullName: ['', [this.message(personNameError)]],
    username: ['', [this.message(usernameError)]],
    email: ['', [this.message(emailError)]],
    password: ['', [this.message(passwordError)]],
    confirmPassword: ['', [this.message((value) => (value ? null : 'Confirma la contraseña.'))]],
    phone: ['', [this.message(phoneError)]],
    documentNumber: ['', [this.message(documentError)]],
    birthDate: ['', [this.message(birthDateError)]],
    role: ['PATIENT', [this.message((value) => (value === 'PATIENT' || value === 'DOCTOR' ? null : 'Selecciona un rol válido: paciente o médico.'))]],
    specialty: ['']
  });

  constructor() {
    this.form.controls.role.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.syncSpecialtyValidator());
    this.form.controls.password.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.syncPasswordConfirmation());
    this.form.controls.confirmPassword.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.syncPasswordConfirmation());
    this.syncSpecialtyValidator();
  }

  submit(): void {
    if (this.loading) {
      return;
    }
    this.formError = '';
    this.syncPasswordConfirmation();
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    this.loading = true;
    this.auth.register({
      fullName: normalizeSpaces(value.fullName),
      username: value.username.trim().toLowerCase(),
      email: value.email.trim().toLowerCase(),
      password: value.password,
      phone: value.phone.trim(),
      documentNumber: value.documentNumber.trim(),
      birthDate: value.birthDate,
      role: value.role as 'PATIENT' | 'DOCTOR',
      specialty: value.role === 'DOCTOR' ? normalizeSpaces(value.specialty) : ''
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/login'], { state: { registered: true } });
      },
      error: (error) => {
        this.loading = false;
        const fields = httpFieldErrors(error);
        this.applyFieldErrors(fields);
        this.formError = Object.keys(fields).length
          ? 'Revisa los datos ingresados.'
          : httpErrorMessage(error, 'No se pudo crear la cuenta.');
      }
    });
  }

  fieldMessage(name: keyof typeof this.form.controls): string {
    const control = this.form.controls[name];
    if (!control.touched && !control.dirty) {
      return '';
    }
    return (control.errors?.['message'] as string) || '';
  }

  private syncPasswordConfirmation(): void {
    const confirm = this.form.controls.confirmPassword;
    const password = this.form.controls.password.value;
    if (!confirm.value) {
      confirm.setErrors({ message: 'Confirma la contraseña.' });
      return;
    }
    if (confirm.value !== password) {
      confirm.setErrors({ message: 'Las contraseñas no coinciden.' });
      return;
    }
    if (confirm.hasError('message')) {
      confirm.setErrors(null);
    }
  }

  private syncSpecialtyValidator(): void {
    const specialty = this.form.controls.specialty;
    if (this.form.controls.role.value === 'DOCTOR') {
      specialty.setValidators([this.message(specialtyError)]);
    } else {
      specialty.clearValidators();
      specialty.setErrors(null);
    }
    specialty.updateValueAndValidity({ emitEvent: false });
  }

  private applyFieldErrors(fields: Record<string, string>): void {
    (Object.keys(this.form.controls) as Array<keyof typeof this.form.controls>).forEach((name) => {
      if (fields[name]) {
        this.form.controls[name].setErrors({ message: fields[name] });
        this.form.controls[name].markAsTouched();
      }
    });
  }

  private message(check: (value: string) => string | null): ValidatorFn {
    return (control: AbstractControl) => {
      const message = check(String(control.value ?? ''));
      return message ? { message } : null;
    };
  }
}
