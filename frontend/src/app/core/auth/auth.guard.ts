import { Component, inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, UserRole } from './auth.service';

@Component({
  selector: 'app-home-redirect',
  standalone: true,
  template: ''
})
export class HomeRedirectComponent {
  constructor() {
    const auth = inject(AuthService);
    const router = inject(Router);
    router.navigateByUrl(auth.isLoggedIn() ? auth.homeRoute() : '/login');
  }
}

export const homeRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return router.createUrlTree([auth.isLoggedIn() ? auth.homeRoute() : '/login']);
};

export function roleGuard(...roles: UserRole[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.isLoggedIn()) {
      return router.createUrlTree(['/login']);
    }
    const role = auth.session()?.role;
    if (!role || !roles.includes(role)) {
      return router.createUrlTree(['/acceso-denegado']);
    }
    return true;
  };
}
