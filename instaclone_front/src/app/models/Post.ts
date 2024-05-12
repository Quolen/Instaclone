import {Comment} from "./Comment";

export interface Post {
  id?: number;
  userId?: number;
  title: string;
  caption: string;
  location: string;
  image?: File;
  likes?: number;
  usersLiked?: string[];
  comments?: Comment[];
  username?: string;
}
