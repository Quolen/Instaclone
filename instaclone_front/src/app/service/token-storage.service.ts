import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root',
})
export class TokenStorageService {
  private isLoggedInSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  isLoggedIn$: Observable<boolean> = this.isLoggedInSubject.asObservable();


  constructor() {}

  public saveToken(token: string): void {
    sessionStorage.setItem(TOKEN_KEY, token);
    this.isLoggedInSubject.next(true);
  }

  public getToken(): string {
    return sessionStorage.getItem(TOKEN_KEY) ?? '';
  }

  public saveUser(user: any): void {
    sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser(): any {
    const user = sessionStorage.getItem(USER_KEY);
    return user ? JSON.parse(user) : null;
  }

  public logOut(): void {
    sessionStorage.clear();
    window.location.reload();
    this.isLoggedInSubject.next(false);
  }
}
