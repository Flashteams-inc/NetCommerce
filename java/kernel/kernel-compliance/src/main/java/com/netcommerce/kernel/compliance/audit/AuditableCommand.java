package com.netcommerce.kernel.compliance.audit;

/** A command whose execution is written to the audit trail. */
public interface AuditableCommand {

    /** The module it belongs to, e.g. "Catalog". */
    String module();

    /** The id of the resource it acts on. */
    String resourceId();
}
