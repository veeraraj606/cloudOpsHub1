package com.cloudopshub.project.Controller;

import com.cloudopshub.project.Dto.ProjectRequest;
import com.cloudopshub.project.Dto.ProjectResponse;
import com.cloudopshub.project.Entity.Project;
import com.cloudopshub.project.Repository.ProjectRepository;
import com.cloudopshub.project.Exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request,
                                                         Authentication authentication) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerUsername(authentication.getName());

        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping
    public List<ProjectResponse> listMyProjects(Authentication authentication) {
        return projectRepository.findByOwnerUsername(authentication.getName())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id, Authentication authentication) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (!project.getOwnerUsername().equals(authentication.getName())) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }

        return ResponseEntity.ok(toResponse(project));
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerUsername(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}