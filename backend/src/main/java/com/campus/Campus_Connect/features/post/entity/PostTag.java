package com.campus.Campus_Connect.features.post.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "post_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Post> posts = new HashSet<>();
}