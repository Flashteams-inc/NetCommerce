package com.netcommerce.kernel.persistence;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;

/**
 * Column names as EF Core gave them: a field's column is its name in PascalCase
 * ("parentCategoryId" -> "ParentCategoryId"), quoted, so the existing schemas
 * are used as they are. A column named in @Column keeps that name exactly (some
 * EF columns are lower case, e.g. catalog.products.price).
 */
public class EfCoreNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    @Override
    public Identifier determineBasicColumnName(ImplicitBasicColumnNameSource source) {
        return pascal(super.determineBasicColumnName(source));
    }

    @Override
    public Identifier determineJoinColumnName(ImplicitJoinColumnNameSource source) {
        return pascal(super.determineJoinColumnName(source));
    }

    private static Identifier pascal(Identifier name) {
        String text = name.getText().replace("_", "");
        return Identifier.toIdentifier(Character.toUpperCase(text.charAt(0)) + text.substring(1), true);
    }
}
