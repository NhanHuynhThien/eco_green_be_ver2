package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.post.PostPackageOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostPackageOptionRepository extends JpaRepository<PostPackageOption, String> {
    List<PostPackageOption> findByPostPackage_IdAndStatusOrderBySortOrderAsc(String packageId, PostPackageOption.Status status);
}
