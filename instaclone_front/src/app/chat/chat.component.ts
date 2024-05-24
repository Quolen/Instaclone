import {Component, ElementRef, OnInit, ViewChild, AfterViewInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import * as Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import {FormControl} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {User} from "../models/User";
import {Message} from "../models/message";
import {UserService} from "../service/user.service";
import {ImageUploadService} from "../service/image-upload.service";
import {ChatService} from "../service/chat.service";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
})
export class ChatComponent implements OnInit, AfterViewInit {
  url = 'http://localhost:8080';
  otherUser?: User;
  thisUser!: User;
  channelName?: string;
  userProfileImage!: File;
  thisUserLoaded: boolean = false;
  otherUserLoaded: boolean = false;
  myProfileImageLoaded: boolean = false;
  myProfileImage!: File;
  stompClient?: Stomp.Client;
  newMessage = new FormControl('');
  messages?: Observable<Array<Message>>;
  connectedToChat = false;
  @ViewChild('chat') chatContainer!: ElementRef<HTMLDivElement>; // ViewChild to access chat element

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private imageService: ImageUploadService,
    private chatService: ChatService,
    private http: HttpClient,
    private el: ElementRef
  ) {
  }

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe((data) => {
      this.thisUser = data;
      this.thisUserLoaded = true;
    });

    this.imageService.getProfileImage()
      .subscribe(image => {
        this.myProfileImage = image.imageBytes;
        this.myProfileImageLoaded = true;
      })

    this.route.params.subscribe((params) => {
      this.userService.getUserById(+params['userId']).subscribe((info) => {
        this.userService.getUserByUsername(info.username).subscribe((data) => {
          this.otherUser = data;
          this.otherUserLoaded = true;
          if (this.otherUser?.id) {
            this.imageService.getProfileImageByUserId(this.otherUser.id).subscribe((image) => {
              this.userProfileImage = image?.imageBytes;
              this.connectToChat();
              this.connectedToChat = true;
              this.el.nativeElement.querySelector('#chat').scrollIntoView();
            });
          }
        });
      });
    });

  }

  ngAfterViewInit(): void {
    this.connectedToChat = true;
    setTimeout(() => {
      if (this.chatContainer && this.chatContainer.nativeElement) {
        this.chatContainer.nativeElement.scrollIntoView();
      }
    }, 0);
  }

  scrollDown(): void {
    if (this.chatContainer) {
      this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
    }
  }

  connectToChat() {
    const id1 = this.thisUser.id!;
    const nick1 = this.thisUser.username;
    const id2 = this.otherUser?.id!;
    const nick2 = this.otherUser?.username!;

    if (id1 > id2) {
      this.channelName = nick1 + '&' + nick2;
    } else {
      this.channelName = nick2 + '&' + nick1;
    }
    this.loadChat();
    console.log('connecting to chat...');

    const socket = new SockJS(this.url + '/chat');
    const headers = {Authorization: sessionStorage.getItem('auth-token')}; // Retrieve JWT token from local storage
    console.log(headers);
    this.stompClient = Stomp.over(socket);
    console.log('Ok');
    this.stompClient.connect(headers, (frame) => {
      console.log('connected to: ' + frame);
      this.stompClient!.subscribe('/topic/messages/' + this.channelName, () => {
        this.loadChat();
        // After loading messages, scroll down
        setTimeout(() => {
          this.scrollDown();
        }, 0);
      });
    });
  }

  sendMsg() {
    if (this.newMessage.value !== '') {
      this.stompClient!.send(
        '/app/chat/' + this.channelName,
        {},
        JSON.stringify({
          sender: this.thisUser.username,
          t_stamp: 'to be defined in server',
          content: this.newMessage.value,
        })
      );
      this.newMessage.setValue('');
    }
  }

  loadChat() {
    this.chatService.getMessages(this.channelName!).subscribe((data) => {
      const msgs: Array<Message> = data;
      msgs.sort((a, b) => (a.ms_id! > b.ms_id! ? 1 : -1));
      this.messages = of(msgs);
    });
    console.log(this.messages);
  }

  whenWasItPublished(myTimeStamp: string) {
    const endDate = myTimeStamp.indexOf('-');
    return myTimeStamp.substring(0, endDate) + ' at ' + myTimeStamp.substring(endDate + 1);
  }

  formatImage(img: any): any {
    if (img == null) {
      return null;
    }
    return 'data:image/jpeg;base64,' + img;
  }
}
