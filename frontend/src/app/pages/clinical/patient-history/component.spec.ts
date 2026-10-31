import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { PatientHistoryComponent } from './component';

describe('PatientHistoryComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [PatientHistoryComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  function createComponent() {
    const fixture = TestBed.createComponent(PatientHistoryComponent);
    fixture.detectChanges();
    return { fixture, component: fixture.componentInstance };
  }

  function withImmediateTimers(action: () => void): void {
    const real = window.setTimeout;
    window.setTimeout = ((handler: TimerHandler) => {
      if (typeof handler === 'function') {
        handler();
      }
      return 0 as unknown as ReturnType<typeof setTimeout>;
    }) as typeof setTimeout;
    try {
      action();
    } finally {
      window.setTimeout = real;
    }
  }

  it('does not request suggestions before the third digit', () => {
    const { component } = createComponent();

    component.documentQuery = '12';
    component.onDocumentInput();

    expect(component.suggestions).toEqual([]);
    httpMock.expectNone((req) => req.url.includes('/patients/suggest'));
  });

  it('lists matches once the document has three digits', () => {
    const { fixture, component } = createComponent();

    component.documentQuery = '123';
    withImmediateTimers(() => component.onDocumentInput());

    const req = httpMock.expectOne((candidate) => candidate.url.includes('/patients/suggest'));
    expect(req.request.params.get('document')).toBe('123');
    req.flush([{ id: 1, fullName: 'Juan Perez', documentNumber: '1234567890' }]);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('1234567890');
    expect(text).toContain('Juan Perez');
  });

  it('blocks the history request when the document is not 6 to 15 digits', () => {
    const { fixture, component } = createComponent();

    component.documentQuery = '12345';
    component.search();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('entre 6 y 15');
    httpMock.expectNone((req) => req.url.includes('/appointments/patient'));
  });

  it('shows the patient history and the total returned by the API', () => {
    const { fixture, component } = createComponent();

    component.documentQuery = '1234567890';
    component.search();

    const req = httpMock.expectOne((candidate) => candidate.url.includes('/appointments/patient'));
    expect(req.request.params.get('document')).toBe('1234567890');
    req.flush({
      patientId: 1,
      patientName: 'Juan Perez',
      patientDocument: '1234567890',
      total: 1,
      appointments: [
        {
          id: 10,
          patientId: 1,
          patientName: 'Juan Perez',
          patientDocument: '1234567890',
          doctorId: 1,
          doctorName: 'Dra. Maria Lopez',
          appointmentDate: '2026-11-01T09:00:00',
          status: 'CONFIRMED',
          whatsappNumber: '3001234567',
          notes: 'Control general'
        }
      ]
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Juan Perez');
    expect(text).toContain('1234567890');
    expect(text).toContain('Total: 1 citas');
    expect(text).toContain('Dra. Maria Lopez');
    expect(text).toContain('Confirmada');
    expect(text).toContain('Control general');
  });

  it('searches the document chosen from a suggestion', () => {
    const { fixture, component } = createComponent();

    component.selectSuggestion({ id: 1, fullName: 'Juan Perez', documentNumber: '1234567890' });

    expect(component.documentQuery).toBe('1234567890');
    const req = httpMock.expectOne((candidate) => candidate.url.includes('/appointments/patient'));
    expect(req.request.params.get('document')).toBe('1234567890');
    req.flush({
      patientId: 1,
      patientName: 'Juan Perez',
      patientDocument: '1234567890',
      total: 1,
      appointments: [
        {
          id: 10,
          patientId: 1,
          doctorId: 1,
          doctorName: 'Dra. Maria Lopez',
          appointmentDate: '2026-11-01T09:00:00',
          status: 'PENDING',
          whatsappNumber: '3001234567',
          notes: ''
        }
      ]
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Pendiente');
  });

  it('says when the patient has no appointments', () => {
    const { fixture, component } = createComponent();

    component.documentQuery = '1234567890';
    component.search();
    httpMock.expectOne((candidate) => candidate.url.includes('/appointments/patient')).flush({
      patientId: 1,
      patientName: 'Juan Perez',
      patientDocument: '1234567890',
      total: 0,
      appointments: []
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Total: 0 citas');
    expect(text).toContain('Este paciente no tiene citas registradas.');
  });
});
