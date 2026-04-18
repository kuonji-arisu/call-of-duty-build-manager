package io.github.kuonjiarisu.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.model.Build;
import io.github.kuonjiarisu.backend.model.BuildDetail;
import io.github.kuonjiarisu.backend.model.BuildSummary;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.model.request.BuildSaveRequest;
import io.github.kuonjiarisu.backend.service.build.BuildCommandService;
import io.github.kuonjiarisu.backend.service.build.BuildQueryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/builds")
public class BuildController {

    private final BuildQueryService buildQueryService;
    private final BuildCommandService buildCommandService;

    public BuildController(BuildQueryService buildQueryService, BuildCommandService buildCommandService) {
        this.buildQueryService = buildQueryService;
        this.buildCommandService = buildCommandService;
    }

    @GetMapping
    public PageResult<BuildSummary> listBuilds(
        Integer page,
        Integer pageSize,
        String keyword,
        String weaponId,
        String generation,
        Boolean favorite
    ) {
        return buildQueryService.listPage(page, pageSize, keyword, weaponId, generation, favorite);
    }

    @GetMapping("/{id}")
    public BuildDetail getBuildDetail(@PathVariable String id) {
        return buildQueryService.findDetail(id);
    }

    @PostMapping
    public Build createBuild(@Valid @RequestBody BuildSaveRequest request) {
        return buildCommandService.create(request.toCommand());
    }

    @PutMapping("/{id}")
    public Build updateBuild(@PathVariable String id, @Valid @RequestBody BuildSaveRequest request) {
        return buildCommandService.update(id, request.toCommand());
    }

    @DeleteMapping("/{id}")
    public void deleteBuild(@PathVariable String id) {
        buildCommandService.delete(id);
    }
}
