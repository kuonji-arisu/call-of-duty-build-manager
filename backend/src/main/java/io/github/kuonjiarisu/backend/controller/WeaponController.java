package io.github.kuonjiarisu.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.model.PageResult;
import io.github.kuonjiarisu.backend.model.WeaponOption;
import io.github.kuonjiarisu.backend.model.request.WeaponSaveRequest;
import io.github.kuonjiarisu.backend.service.WeaponService;

@RestController
@RequestMapping("/api/admin/weapons")
public class WeaponController {

    private final WeaponService weaponService;

    public WeaponController(WeaponService weaponService) {
        this.weaponService = weaponService;
    }

    @GetMapping
    public PageResult<Weapon> list(
        Integer page,
        Integer pageSize,
        String keyword,
        String weaponType,
        String generation,
        Boolean favorite
    ) {
        return weaponService.listPage(page, pageSize, keyword, weaponType, generation, favorite);
    }

    @GetMapping("/options")
    public List<WeaponOption> listOptions() {
        return weaponService.listOptions();
    }

    @GetMapping("/search-options")
    public PageResult<WeaponOption> searchOptions(Integer page, Integer pageSize, String keyword) {
        return weaponService.searchOptions(page, pageSize, keyword);
    }

    @PostMapping
    public Weapon create(@RequestBody WeaponSaveRequest request) {
        return weaponService.create(request.toCommand());
    }

    @PutMapping("/{id}")
    public Weapon update(@PathVariable String id, @RequestBody WeaponSaveRequest request) {
        return weaponService.update(id, request.toCommand());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        weaponService.delete(id);
    }
}
