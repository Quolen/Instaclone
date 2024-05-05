import {Inject, Injectable, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser} from "@angular/common";

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root',
})
export class TokenStorageService {
  constructor(@Inject(PLATFORM_ID) public platformId: Object) {}  // No setup required

  public saveToken(token: string): void {
    sessionStorage.setItem(TOKEN_KEY, token);
  }

  public getToken(): string {
    return sessionStorage.getItem(TOKEN_KEY) ?? '';
  }

  public saveUser(user: any): void {
    sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser(): any {
    if (isPlatformBrowser(this.platformId)) {  // Check if running in a browser
      const user = sessionStorage.getItem(USER_KEY);
      return user ? JSON.parse(user) : null;
    }
    return null;  // Handle non-browser environments
  }

  public logOut(): void {
    sessionStorage.clear();
    window.location.reload();
  }
}
