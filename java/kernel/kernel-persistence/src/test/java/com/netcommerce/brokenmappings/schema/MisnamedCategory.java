package com.netcommerce.brokenmappings.schema;

import java.util.UUID;

import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A mapping that does not match the schema: its column "Title" does not exist. */
@jakarta.persistence.Entity
@Table(name = "categories", schema = "catalog")
public class MisnamedCategory {

    @Id
    private UUID id;
    private String title;
}
