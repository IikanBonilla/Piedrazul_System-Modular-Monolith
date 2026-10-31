import { Routes } from '@angular/router';

export const DOCTOR_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./home/component').then((m) => m.DoctorHomeComponent)
  },
  {
    path: 'citas',
    loadComponent: () =>
      import('../scheduler/appointment-list/component').then((m) => m.AppointmentListComponent)
  },
  {
    path: 'historial',
    loadComponent: () =>
      import('../clinical/patient-history/component').then((m) => m.PatientHistoryComponent)
  }
];
