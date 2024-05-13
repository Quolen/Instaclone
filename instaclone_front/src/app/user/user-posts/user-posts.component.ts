import {Component, OnInit} from '@angular/core';
import {PostService} from "../../service/post.service";
import {Post} from "../../models/Post";
import {ImageUploadService} from "../../service/image-upload.service";
import {CommentService} from "../../service/comment.service";
import {UserService} from "../../service/user.service";
import {NotificationService} from "../../service/notification.service";
import {ProfilePictureService} from "../../service/profile-picture.service";

@Component({
  selector: 'app-user-posts',
  templateUrl: './user-posts.component.html',
  styleUrl: './user-posts.component.css'
})
export class UserPostsComponent implements OnInit{

  arePostsLoaded = false;
  imageLoaded = false;
  posts!: Post[];
  userProfileImage!: File;

  constructor(private userService: UserService,
              private postService: PostService,
              private commentService: CommentService,
              private notificationService: NotificationService,
              private imageService: ImageUploadService,
              private profilePictureService: ProfilePictureService) {
  }

  ngOnInit(): void {
    this.profilePictureService.profilePictureUpdated$
      .subscribe(() => {
        this.imageService.getProfileImage()
          .subscribe(data => {
            this.userProfileImage = data.imageBytes;
            this.imageLoaded = true;
          });
      })
    this.postService.getPostForCurrentUser()
      .subscribe(data => {
        console.log(data);
        this.posts = data;
        this.getImagesToPosts(this.posts);
        this.getCommentsToPosts(this.posts);
        this.arePostsLoaded = true;
      });
  }

  getImagesToPosts(posts: Post[]): void {
    posts.forEach(post => {
      if (post.id !== undefined) {
        this.imageService.getImageToPost(post.id)
          .subscribe(data => {
            post.image = data.imageBytes;
          })
      }
    });
  }

  getCommentsToPosts(posts: Post[]): void {
    posts.forEach(post => {
      if (post.id !== undefined) {
        this.commentService.getCommentsToPost(post.id)
          .subscribe(data => {
            post.comments = data;
          })
      }
    });
  }

  deletePost(post: Post, index: number): void {
    console.log(post);
    const result = confirm("Are you sure you want to remove this post?");
    if (result) {
      if (post.id !== undefined) {
        this.postService.delete(post.id)
          .subscribe(() => {
            this.posts.splice(index, 1);
            this.notificationService.showSnackBar("Post deleted successfully.");
          });
      }
    }
  }

  deleteComment(commentId: number, postIndex: number, commentIndex: number): void {
    const post = this.posts[postIndex];

    this.commentService.delete(commentId)
      .subscribe(() => {
        this.notificationService.showSnackBar('Comment deleted successfully.');
        post.comments?.splice(commentIndex, 1);
      });
  }

  formatImage(img: any): any {
    if (img == null) {
      return null;
    }
    return 'data:image/jpeg;base64,' + img;
  }
}
