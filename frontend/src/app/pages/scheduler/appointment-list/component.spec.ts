import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AppointmentListComponent } from './component';

describe('AppointmentListComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AppointmentListComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.match('http://localhost:8080/api/v1/doctors').forEach((req) => req.flush([]));
    httpMock.verify();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(AppointmentListComponent);
    fixture.detectChanges();
    return { fixture, component: fixture.componentInstance };
  }

  function stubDownload(downloads: string[]): () => void {
    const originalCreate = URL.createObjectURL;
    const originalRevoke = URL.revokeObjectURL;
    const originalClick = HTMLAnchorElement.prototype.click;
    URL.createObjectURL = () => 'blob:citas';
    URL.revokeObjectURL = () => undefined;
    HTMLAnchorElement.prototype.click = function (this: HTMLAnchorElement) {
      downloads.push(this.download);
    };
    return () => {
      URL.createObjectURL = originalCreate;
      URL.revokeObjectURL = originalRevoke;
      HTMLAnchorElement.prototype.click = originalClick;
    };
  }

  it('does not export until a doctor and a date are selected', () => {
    const { fixture, component } = createComponent();

    component.exportCsv();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Selecciona un médico o terapista.');
    expect(fixture.nativeElement.textContent).toContain('Selecciona una fecha.');
    httpMock.expectNone((req) => req.url.includes('/export'));
  });

  it('downloads the day list as citas-{doctorId}-{date}.csv', () => {
    const { component } = createComponent();
    const downloads: string[] = [];
    const restore = stubDownload(downloads);
    component.selectedDoctorId = 7;
    component.selectedDate = '2026-10-31';

    try {
      component.exportCsv();
      const req = httpMock.expectOne(
        'http://localhost:8080/api/v1/appointments/doctor/7/export?date=2026-10-31'
      );
      expect(req.request.method).toBe('GET');
      expect(req.request.responseType).toBe('blob');
      req.flush(new Blob(['Médico;Dra. Maria Lopez'], { type: 'text/csv' }));
      expect(downloads).toEqual(['citas-7-2026-10-31.csv']);
    } finally {
      restore();
    }
  });

  it('shows the API message when the CSV export fails', async () => {
    const { fixture, component } = createComponent();
    component.selectedDoctorId = 99;
    component.selectedDate = '2026-10-31';

    component.exportCsv();
    const req = httpMock.expectOne(
      'http://localhost:8080/api/v1/appointments/doctor/99/export?date=2026-10-31'
    );
    req.flush(
      new Blob([JSON.stringify({ message: 'No existe el médico o terapista seleccionado.' })], {
        type: 'application/json'
      }),
      { status: 404, statusText: 'Not Found' }
    );
    await new Promise((resolve) => setTimeout(resolve, 0));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No existe el médico o terapista seleccionado.');
  });
});
