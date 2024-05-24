import { Component, OnInit } from '@angular/core';
import { User } from "../../models/User";
import { TokenStorageService } from "../../service/token-storage.service";
import { PostService } from "../../service/post.service";
import { MatDialog, MatDialogConfig } from "@angular/material/dialog";
import { NotificationService } from "../../service/notification.service";
import { ImageUploadService } from "../../service/image-upload.service";
import { UserService } from "../../service/user.service";
import { EditUserComponent } from "../edit-user/edit-user.component";
import { ProfilePictureService } from "../../service/profile-picture.service";
import { ActivatedRoute, Router } from "@angular/router";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {

  isUserDataLoaded = false;
  user!: User;
  userId!: number;
  selectedFile: File | null = null;
  userProfileImage: File | null = null;
  previewImgURL: any;
  isProfileOwner: boolean = false;
  hasProfileImage!: boolean;

  constructor(private tokenService: TokenStorageService,
              private postService: PostService,
              private dialog: MatDialog,
              private notificationService: NotificationService,
              private imageService: ImageUploadService,
              private userService: UserService,
              private profilePictureService: ProfilePictureService,
              private route: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.userId = +params['userId'];
      this.userService.getUserById(this.userId)
        .subscribe(data => {
          this.user = data;
          this.userService.getCurrentUser()
            .subscribe(data => {
              this.isProfileOwner = data.id === this.userId;
              this.isUserDataLoaded = true;
            })
        });
      this.imageService.getProfileImageByUserId(this.userId)
        .subscribe(data => {
          this.userProfileImage = data.imageBytes;
          this.hasProfileImage = !!this.userProfileImage;
        });
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];

    if (this.selectedFile) {
      const reader = new FileReader();
      reader.readAsDataURL(this.selectedFile);
      reader.onload = () => {
        this.previewImgURL = reader.result;
      };
    }
  }

  openEditDialog(): void {
    const dialogUserEditConfig = new MatDialogConfig();
    dialogUserEditConfig.width = '400px';
    dialogUserEditConfig.data = {
      user: this.user
    }
    this.dialog.open(EditUserComponent, dialogUserEditConfig);
  }

  onUpload(): void {
    if (this.selectedFile != null) {
      this.imageService.uploadImageToUser(this.selectedFile)
        .subscribe(() => {
          this.notificationService.showSnackBar("Profile Image updated successfully")
          this.selectedFile = null;
          this.hasProfileImage = true;
          this.profilePictureService.notifyProfilePictureUpdated();
        });
    }
  }

  deleteProfileImage() {
    this.imageService.deleteProfileImage().subscribe(() => {
      this.notificationService.showSnackBar("Profile Image deleted successfully");
      this.selectedFile = null;
      this.userProfileImage = null;
      this.hasProfileImage = false;
      this.profilePictureService.notifyProfilePictureUpdated();
      window.location.reload();
    });
  }

  startChat(): void {
    this.router.navigate(['/chat', this.userId]);
  }

  formatImage(img: any): any {
    if (img == null) {
      return null;
    }
    return 'data:image/jpeg;base64,' + img;
  }
}
