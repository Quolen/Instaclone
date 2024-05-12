import {Component, OnInit} from '@angular/core';
import {Post} from "../../models/Post";
import {User} from "../../models/User";
import {UserService} from "../../service/user.service";
import {PostService} from "../../service/post.service";
import {CommentService} from "../../service/comment.service";
import {NotificationService} from "../../service/notification.service";
import {ImageUploadService} from "../../service/image-upload.service";

@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrl: './index.component.css'
})
export class IndexComponent implements OnInit {

  isPostsLoaded = false;
  posts!: Post[];

  isUserDataLoaded = false;
  user!: User;

  postProfileImages: Map<number, string> = new Map<number, string>(); // Map to store post user profile images


  constructor(private userService: UserService,
              private postService: PostService,
              private commentService: CommentService,
              private notificationService: NotificationService,
              private imageService: ImageUploadService) {
  }

  ngOnInit(): void {
    this.postService.getAllPosts()
      .subscribe(data => {
        console.log(data);
        this.posts = data;
        this.getImagesToPosts(this.posts);
        this.getCommentsToPosts(this.posts);
        this.isPostsLoaded = true;
      })

    this.userService.getCurrentUser()
      .subscribe(data => {
        this.user = data;
        this.isUserDataLoaded = true;
      })
  }

  getImagesToPosts(posts: Post[]): void {
    posts.forEach(post => {
      if (post.userId !== undefined) {
        this.imageService.getProfileImageToPost(post.userId)
          .subscribe(data => {
            this.postProfileImages.set(post.userId ?? -1, 'data:image/jpeg;base64,' + data.imageBytes);
          })
      }
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

  likePost(postId: number, postIndex: number): void {
    const post = this.posts[postIndex];
    console.log(post);

    if (post.usersLiked === undefined) {
      post.usersLiked = [];  // Ensure `userLiked` is initialized
    }

    if (!post.usersLiked.includes(this.user.username)) {
      this.postService.likePost(postId, this.user.username)
        .subscribe({
          next: () => {
            post.usersLiked?.push(this.user.username);
            this.notificationService.showSnackBar('Liked!');
          },
          error: (err) => {
            console.error('Error liking post:', err);
            this.notificationService.showSnackBar('Error liking post.');
          }
        });
    } else {
      this.postService.likePost(postId, this.user.username)
        .subscribe({
          next: () => {
            const index = post.usersLiked?.indexOf(this.user.username);
            if (index !== undefined && index > -1) {
              post.usersLiked?.splice(index, 1);
            }
            this.notificationService.showSnackBar('Unliked!');
          },
          error: (err) => {
            console.error('Error unliking post:', err);
            this.notificationService.showSnackBar('Error unliking post.');
          }
        });
    }
  }

  postComment(message: string, postId: number, postIndex: number): void {
    const post = this.posts[postIndex];
    console.log(post);
    this.commentService.addCommentToPost(postId, message)
      .subscribe(data => {
        console.log(data);
        post.comments?.unshift(data);
      });
  }

  formatImage(img: any): any {
    if (img == null) {
      return null;
    }
    return 'data:image/jpeg;base64,' + img;
  }
}
