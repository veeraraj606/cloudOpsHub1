package com.cloudopshub.project.Repository;

import com.cloudopshub.project.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerUsername(String ownerUsername);
}