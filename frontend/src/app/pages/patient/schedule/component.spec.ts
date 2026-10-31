import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { PatientScheduleComponent } from './component';

describe('PatientScheduleComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientScheduleComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    const doctors = httpMock.match('http://localhost:8080/api/v1/doctors');
    doctors.forEach((req) => req.flush([]));
    httpMock.verify();
  });

  it('should require doctor and date before consulting availability', () => {
    const fixture = TestBed.createComponent(PatientScheduleComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.consultAvailability();
    expect(component.formError).toContain('medico');

    component.selectedDoctorId = 1;
    component.consultAvailability();
    expect(component.formError).toContain('fecha');
  });

  it('should ask for confirmation before scheduling', () => {
    const fixture = TestBed.createComponent(PatientScheduleComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.documentNumber = '1234567890';
    component.selectedDoctorId = 1;
    component.selectedSlot = '2026-09-21T11:00:00';

    component.requestSchedule();

    expect(component.showConfirmModal).toBe(true);
    httpMock.expectNone('http://localhost:8080/api/v1/appointments');
  });
});
