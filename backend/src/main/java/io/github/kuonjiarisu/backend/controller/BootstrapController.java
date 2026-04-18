package io.github.kuonjiarisu.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.model.InitialData;
import io.github.kuonjiarisu.backend.service.LibraryService;

@RestController
@RequestMapping("/api/public/bootstrap")
public class BootstrapController {

    private final LibraryService libraryService;

    public BootstrapController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public InitialData loadInitialData() {
        return libraryService.loadInitialData();
    }
}
