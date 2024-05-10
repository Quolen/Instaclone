import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Post} from "../../models/Post";
import {NotificationService} from "../../service/notification.service";
import {PostService} from "../../service/post.service";
import {Router} from "@angular/router";
import {ImageUploadService} from "../../service/image-upload.service";

@Component({
  selector: 'app-add-post',
  templateUrl: './add-post.component.html',
  styleUrl: './add-post.component.css'
})
export class AddPostComponent implements OnInit{

  postForm!: FormGroup;
  selectedFile!: File;
  isPostCreated = false;
  createdPost!: Post;
  previewImgURL: any;

  constructor(private fb: FormBuilder,
              private notificationService: NotificationService,
              private postService: PostService,
              private router: Router,
              private imageService: ImageUploadService,) {
  }

  ngOnInit(): void {
    this.postForm = this.createPostForm();
  }

  createPostForm(): FormGroup {
    return this.fb.group({
      title: ['', Validators.required],
      location: ['', Validators.required],
      caption: ['', Validators.required]
    });
  }

  submit(): void {
    this.postService.createPost({
      title: this.postForm.value.title,
      caption: this.postForm.value.caption,
      location: this.postForm.value.location
    }).subscribe(data => {
      this.createdPost = data;
      console.log('Post created');
      console.log(data);

      if (this.createdPost.id !== undefined) {
        this.imageService.uploadImageToPost(this.selectedFile, this.createdPost.id)
        .subscribe(() => {
          this.notificationService.showSnackBar('Post created successfully');
          this.isPostCreated = true;
          this.router.navigate(['/profile']);
        });
      }
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];

    const reader = new FileReader();
    reader.readAsDataURL(this.selectedFile);
    reader.onload = () => {
      this.previewImgURL = reader.result;
    };
  }
}
