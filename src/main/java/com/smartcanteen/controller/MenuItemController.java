package com.smartcanteen.controller;

import com.smartcanteen.model.MenuItem;
import com.smartcanteen.service.MenuItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    /**
     * GET /api/menu or /api/menu?category=Snacks
     * Returns all active food items available to students.
     */
    @GetMapping
    public ResponseEntity<List<MenuItem>> getMenu(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(menuItemService.getMenuItemsByCategory(category));
        }
        return ResponseEntity.ok(menuItemService.getAllActiveMenuItems());
    }

    /**
     * GET /api/menu/categories
     * Returns list of distinct categories for filtering.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(menuItemService.getCategories());
    }

    /**
     * GET /api/menu/{id}
     * Returns a single menu item by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(id));
    }
}
