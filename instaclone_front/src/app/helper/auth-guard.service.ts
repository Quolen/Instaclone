import { Injectable } from '@angular/core';
import { CanMatchFn, Route, UrlSegment, Router } from '@angular/router';
import { TokenStorageService } from '../service/token-storage.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuardService {
  constructor(private router: Router, private tokenService: TokenStorageService) {}

  canMatch: CanMatchFn = (route: Route, segments: UrlSegment[]): boolean => {
    const currentUser = this.tokenService.getUser();
    if (currentUser) {
      return true;  // User has access
    }

    this.router.navigate(['/login']);  // Redirect if not logged in
    return false;  // Block the route
  };
}
