package com.smartcanteen.repository;

import com.smartcanteen.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByActiveTrue();

    List<MenuItem> findByActiveTrueAndCategoryIgnoreCase(String category);

    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.active = true ORDER BY m.category")
    List<String> findDistinctCategories();

    List<MenuItem> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
