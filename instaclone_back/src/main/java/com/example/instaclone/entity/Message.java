package com.example.instaclone.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import jakarta.persistence.*;
import lombok.ToString;

@Data
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ms_id")
    private Long ms_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    @ToString.Exclude
    private Chat chat;

    @Column(name = "sender")
    private String sender;

    @Column(name = "t_stamp")
    private String t_stamp;

    @Column(name = "content")
    private String content;

    public Message() {
    }

    public Message(String sender, String t_stamp, String content, Chat chat) {
        this.sender = sender;
        this.t_stamp = t_stamp;
        this.content = content;
        this.chat = chat;
    }
}
