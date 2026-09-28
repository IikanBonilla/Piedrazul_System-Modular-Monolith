import { Routes } from '@angular/router';

export const PATIENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./home/component').then((m) => m.PatientHomeComponent)
  },
  {
    path: 'agendar',
    loadComponent: () =>
      import('./schedule/component').then((m) => m.PatientScheduleComponent)
  },
  {
    path: 'disponibilidad',
    loadComponent: () =>
      import('./available-slots/component').then((m) => m.AvailableSlotsComponent)
  }
];
