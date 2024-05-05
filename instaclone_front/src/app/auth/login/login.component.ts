import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../service/auth.service";
import {TokenStorageService} from "../../service/token-storage.service";
import {NotificationService} from "../../service/notification.service";
import {Router} from "@angular/router";
import {isPlatformBrowser} from "@angular/common";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {

  public loginForm!: FormGroup;

  constructor(
    private authService: AuthService,
    private tokenStorage: TokenStorageService,
    private notificationService: NotificationService,
    private router: Router,
    private fb: FormBuilder,
  ) {
    if (isPlatformBrowser(this.tokenStorage.platformId)) {  // Ensure browser context
      if (this.tokenStorage.getUser()) {
        this.router.navigate(['main']);  // Redirect if user is logged in
      }
    }
  }

  ngOnInit(): void {
    this.loginForm = this.createLoginForm();
  }

  createLoginForm(): FormGroup {
    return this.fb.group({
      username: ['', Validators.compose([Validators.required, Validators.email])],
      password: ['', Validators.required],
    })
  }

  submit(): void {
    this.authService
      .login({
        username: this.loginForm.value.username,
        password: this.loginForm.value.password,
      })
      .subscribe({
        next: (data) => {
          console.log(data);

          this.tokenStorage.saveToken(data.token);
          this.tokenStorage.saveUser(data);

          this.notificationService.showSnackBar('Successfully logged in');
          this.router.navigate(['/']);
          window.location.reload();
        },
        error: (error) => {
          console.log(error);
          this.notificationService.showSnackBar('Login error');
        },
      });
  }
}
