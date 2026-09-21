import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { PatientRegisterComponent } from './component';

describe('PatientRegisterComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientRegisterComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should block incomplete registration and list missing fields', () => {
    const fixture = TestBed.createComponent(PatientRegisterComponent);
    const component = fixture.componentInstance;

    component.register();

    expect(component.formError).toContain('nombre de usuario');
    expect(component.formError).toContain('contrasena');
    expect(component.successMessage).toBe('');
    httpMock.expectNone('http://localhost:8080/api/v1/patients/register');
  });

  it('should show success message when registration succeeds', () => {
    const fixture = TestBed.createComponent(PatientRegisterComponent);
    const component = fixture.componentInstance;
    component.username = 'ana';
    component.password = 'clave123';
    component.fullName = 'Ana Ruiz';
    component.documentNumber = '1098765432';
    component.phone = '3009998877';

    component.register();

    const req = httpMock.expectOne('http://localhost:8080/api/v1/patients/register');
    req.flush({
      id: 15,
      username: 'ana',
      fullName: 'Ana Ruiz',
      documentNumber: '1098765432',
      phone: '3009998877',
      message: 'Registro exitoso. El paciente ya puede agendar citas.'
    });

    expect(component.successMessage).toContain('Registro exitoso');
  });
});
