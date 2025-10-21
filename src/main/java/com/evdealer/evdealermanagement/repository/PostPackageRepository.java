package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.post.PostPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostPackageRepository extends JpaRepository<PostPackage, String> {
    List<PostPackage> findByStatusOrderByPriorityLevelDesc(PostPackage.Status status);
}
