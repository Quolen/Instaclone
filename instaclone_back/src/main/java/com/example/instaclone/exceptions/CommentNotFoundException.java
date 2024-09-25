package com.example.instaclone.exceptions;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String commentNotFound) {
        super(commentNotFound);
    }
}
