import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { SchedulingConfigComponent } from './component';

describe('SchedulingConfigComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SchedulingConfigComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.match('http://localhost:8080/api/v1/doctors').forEach((req) => req.flush([]));
    httpMock.verify();
  });

  function createComponent(): SchedulingConfigComponent {
    const fixture = TestBed.createComponent(SchedulingConfigComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.selectedDoctorId = 1;
    return component;
  }

  it('should reject invalid weeks before calling the API', () => {
    const component = createComponent();
    component.bookingWindowWeeks = 0;
    component.save();
    expect(component.formError).toContain('semanas');
    httpMock.expectNone((req) => req.url.includes('scheduling-config') && req.method === 'PUT');
  });

  it('should reject when no working days are selected', () => {
    const component = createComponent();
    Object.keys(component.selectedDays).forEach((day) => component.selectedDays[day] = false);
    component.save();
    expect(component.formError).toContain('al menos un dia');
    httpMock.expectNone((req) => req.method === 'PUT');
  });

  it('should reject invalid time range and interval', () => {
    const component = createComponent();
    component.timeSlots = [{ startTime: '18:00', endTime: '08:00' }];
    component.save();
    expect(component.formError).toContain('hora de inicio');

    component.timeSlots = [{ startTime: '08:00', endTime: '12:00' }];
    component.slotIntervalMinutes = 0;
    component.save();
    expect(component.formError).toContain('intervalo');
  });
});
