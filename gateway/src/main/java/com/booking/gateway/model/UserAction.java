package com.booking.gateway.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_actions")
public class UserAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String user;
    private String method;
    private String path;
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public UserAction() {}
    public UserAction(String user, String method, String path, Date timestamp) {
        this.user = user;
        this.method = method;
        this.path = path;
        this.timestamp = timestamp;
    }
    // Getters and setters omitted for brevity
} 