import {inject} from '@angular/core';
import {Router, CanActivateFn} from '@angular/router';
import { TokenStorageService } from '../service/token-storage.service';

export const AuthGuardService: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const tokenService = inject(TokenStorageService);

  const currentUser = tokenService.getUser();
  if (currentUser) {
    return true;  // User has access
  }
  return router.createUrlTree(['/login']);  // Redirect if not logged in
};
