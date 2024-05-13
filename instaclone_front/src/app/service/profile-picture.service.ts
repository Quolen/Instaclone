import { Injectable } from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ProfilePictureService {

  private profilePictureSubject = new BehaviorSubject<boolean>(false);

  profilePictureUpdated$ = this.profilePictureSubject.asObservable();

  notifyProfilePictureUpdated() {
    this.profilePictureSubject.next(true);
  }
}
