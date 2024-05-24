import { Injectable } from '@angular/core';
import {BehaviorSubject, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ProfilePictureService {
  private profilePictureUpdatedSubject = new Subject<void>();

  profilePictureUpdated$ = this.profilePictureUpdatedSubject.asObservable();

  notifyProfilePictureUpdated() {
    this.profilePictureUpdatedSubject.next();
  }
}
