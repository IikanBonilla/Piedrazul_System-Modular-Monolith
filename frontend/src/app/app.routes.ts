import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'agendador', pathMatch: 'full' },
  {
    path: 'agendador',
    loadChildren: () =>
      import('./pages/scheduler/routes').then((m) => m.SCHEDULER_ROUTES)
  }
];
