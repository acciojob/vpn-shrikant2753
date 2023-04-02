package com.driver.model;

import javax.naming.Name;
import javax.persistence.*;

@Entity
@Table(name = "connection")
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //many to
    //children of service provider
    @ManyToOne
    @JoinColumn
    private ServiceProvider serviceProvider;

    //many to
    //children of user
    @ManyToOne
    @JoinColumn
    private User user;
}
