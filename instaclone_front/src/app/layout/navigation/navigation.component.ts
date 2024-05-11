import {Component, OnInit} from '@angular/core';
import {User} from "../../models/User";
import {TokenStorageService} from "../../service/token-storage.service";
import {UserService} from "../../service/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.css'
})
export class NavigationComponent implements OnInit {

  isLoggedIn = false;
  isDataLoaded = false;
  user!: User;

  constructor(private tokenStorage: TokenStorageService,
              private userService: UserService,
              private router: Router,) {
  }

  ngOnInit(): void {
    this.tokenStorage.isLoggedIn$.subscribe(isLoggedIn => {
      this.isLoggedIn = isLoggedIn;
    });

    if (this.isLoggedIn) {
      this.userService.getCurrentUser()
        .subscribe(data => {
          this.user = data;
          this.isDataLoaded = true;
        })
    }
  }

  logout(): void {
    this.tokenStorage.logOut();
    this.router.navigate(['/login']);
  }

}
