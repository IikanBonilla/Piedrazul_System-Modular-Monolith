import { Routes } from '@angular/router';

export const PATIENT_ROUTES: Routes = [
  {
    path: 'registro',
    loadComponent: () =>
      import('./register/component').then((m) => m.PatientRegisterComponent)
  },
  {
    path: 'agendar',
    loadComponent: () =>
      import('./schedule/component').then((m) => m.PatientScheduleComponent)
  }
];
