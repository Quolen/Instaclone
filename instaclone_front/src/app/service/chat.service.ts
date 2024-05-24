import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private baseUrl = 'http://localhost:8080/';

  constructor(private http: HttpClient) {
  }

  getMessages(channelName: string): Observable<any> {
    return this.http.post(this.baseUrl + 'getMessages', {chat: channelName});
  }
}
