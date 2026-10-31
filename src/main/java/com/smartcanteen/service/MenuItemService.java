package com.smartcanteen.service;

import com.smartcanteen.dto.MenuItemRequest;
import com.smartcanteen.exception.ResourceNotFoundException;
import com.smartcanteen.model.MenuItem;
import com.smartcanteen.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public List<MenuItem> getAllActiveMenuItems() {
        return menuItemRepository.findByActiveTrue();
    }

    public List<MenuItem> getMenuItemsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("all") || category.isBlank()) {
            return getAllActiveMenuItems();
        }
        return menuItemRepository.findByActiveTrueAndCategoryIgnoreCase(category);
    }

    public List<String> getCategories() {
        return menuItemRepository.findDistinctCategories();
    }

    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + id));
    }

    public List<MenuItem> getAllMenuItemsForAdmin() {
        return menuItemRepository.findAll();
    }

    @Transactional
    public MenuItem createMenuItem(MenuItemRequest request) {
        MenuItem item = new MenuItem(
            request.getName().trim(),
            request.getDescription() != null ? request.getDescription().trim() : "",
            request.getCategory().trim(),
            request.getPrice(),
            request.getImageUrl() != null && !request.getImageUrl().isBlank()
                ? request.getImageUrl().trim()
                : "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&q=80",
            request.isAvailable(),
            request.getPreparationTimeMinutes() > 0 ? request.getPreparationTimeMinutes() : 5
        );
        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem item = getMenuItemById(id);
        item.setName(request.getName().trim());
        item.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        item.setCategory(request.getCategory().trim());
        item.setPrice(request.getPrice());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            item.setImageUrl(request.getImageUrl().trim());
        }
        item.setAvailable(request.isAvailable());
        if (request.getPreparationTimeMinutes() > 0) {
            item.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        }
        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem toggleAvailability(Long id) {
        MenuItem item = getMenuItemById(id);
        item.setAvailable(!item.isAvailable());
        return menuItemRepository.save(item);
    }

    @Transactional
    public MenuItem toggleActive(Long id) {
        MenuItem item = getMenuItemById(id);
        item.setActive(!item.isActive());
        return menuItemRepository.save(item);
    }
}
