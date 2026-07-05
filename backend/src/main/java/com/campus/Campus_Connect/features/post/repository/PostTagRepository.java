package com.campus.Campus_Connect.features.post.repository;

import com.campus.Campus_Connect.features.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Integer> {

    Optional<PostTag> findByName(String name);

    List<PostTag> findAllByOrderByNameAsc();

}