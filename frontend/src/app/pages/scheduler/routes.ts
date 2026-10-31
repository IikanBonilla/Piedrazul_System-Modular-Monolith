import { Routes } from '@angular/router';

export const SCHEDULER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./appointment-list/component').then(m => m.AppointmentListComponent)
  },
  {
    path: 'historial',
    loadComponent: () =>
      import('../clinical/patient-history/component').then((m) => m.PatientHistoryComponent)
  }
];
