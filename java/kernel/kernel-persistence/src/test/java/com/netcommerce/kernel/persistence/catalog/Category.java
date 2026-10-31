package com.netcommerce.kernel.persistence.catalog;

import com.netcommerce.kernel.core.domain.AggregateRoot;

import jakarta.persistence.Table;

/** catalog.categories as EF Core created it, mapped with the kernel's conventions only. */
@jakarta.persistence.Entity
@Table(name = "categories", schema = "catalog")
public class Category extends AggregateRoot<CategoryId> {

    private String name;
    private String description;
    private String slug;
    private CategoryId parentCategoryId;
    private int displayOrder;
    private boolean isActive;
    private String imageKey;

    protected Category() {
    }

    public Category(CategoryId id, String name, String slug, int displayOrder, boolean active) {
        super(id);
        this.name = name;
        this.description = "";
        this.slug = slug;
        this.displayOrder = displayOrder;
        this.isActive = active;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
}
