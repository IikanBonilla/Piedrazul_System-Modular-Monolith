import { Component, inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidatorFn } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { httpErrorMessage, httpFieldErrors } from '../../../core/http/error-message';
import { usernameError } from '../../../core/validation/validators';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './component.html'
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loading = false;
  showPassword = false;
  formError = '';
  registered = false;
  sessionExpired = false;

  readonly form = this.fb.nonNullable.group({
    username: ['', [this.message(usernameError)]],
    password: ['', [this.message((value) => (value.trim() ? null : 'Este campo es obligatorio.'))]]
  });

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.router.navigateByUrl(this.auth.homeRoute());
    }
    this.registered = history.state?.registered === true;
    this.sessionExpired = this.route.snapshot.queryParamMap.get('reason') === 'sesion';
  }

  submit(): void {
    if (this.loading) {
      return;
    }
    this.formError = '';
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    const { username, password } = this.form.getRawValue();
    this.auth.login({ username: username.trim().toLowerCase(), password }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigateByUrl(this.auth.homeRoute());
      },
      error: (error) => {
        this.loading = false;
        const fields = httpFieldErrors(error);
        this.applyFieldErrors(fields);
        this.formError = Object.keys(fields).length
          ? ''
          : httpErrorMessage(error, 'No se pudo iniciar sesión.');
      }
    });
  }

  fieldMessage(name: 'username' | 'password'): string {
    const control = this.form.controls[name];
    if (!control.touched && !control.dirty) {
      return '';
    }
    return (control.errors?.['message'] as string) || '';
  }

  private applyFieldErrors(fields: Record<string, string>): void {
    (['username', 'password'] as const).forEach((name) => {
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
