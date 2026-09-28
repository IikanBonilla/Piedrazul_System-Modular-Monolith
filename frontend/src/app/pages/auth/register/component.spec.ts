import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RegisterComponent } from './component';

describe('RegisterComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should block incomplete registration before calling the unified endpoint', () => {
    const fixture = TestBed.createComponent(RegisterComponent);
    const component = fixture.componentInstance;

    component.submit();

    expect(component.form.invalid).toBe(true);
    expect(component.form.controls.username.errors?.['message']).toContain('obligatorio');
    expect(component.form.controls.password.errors?.['message']).toContain('obligatorio');
    httpMock.expectNone('http://localhost:8080/api/v1/auth/register');
    httpMock.expectNone('http://localhost:8080/api/v1/patients/register');
  });

  it('should register a patient through the unified auth endpoint', () => {
    const fixture = TestBed.createComponent(RegisterComponent);
    const component = fixture.componentInstance;
    component.form.setValue({
      fullName: 'Ana Ruiz',
      username: 'ana.ruiz',
      email: 'ana@example.com',
      password: 'clave123',
      confirmPassword: 'clave123',
      phone: '3009998877',
      documentNumber: '1098765432',
      birthDate: '1998-03-04',
      role: 'PATIENT',
      specialty: ''
    });

    component.submit();

    const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.role).toBe('PATIENT');
    expect(req.request.body.username).toBe('ana.ruiz');
    expect(req.request.body.email).toBe('ana@example.com');
    req.flush({ message: 'Cuenta creada. Ya puedes iniciar sesión.' });
  });
});
