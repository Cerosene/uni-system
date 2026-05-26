// DEPRECATED: This enum has been moved to pl.usos2.server.model.enumtype.UserRole
// Use that location instead. This file is kept for backward compatibility but should not be used.
//
// The project uses a single UserRole enum in the server model package to avoid duplication
// and ensure consistency across client and server code.

package pl.usos2.client.app;

/**
 * @deprecated Use pl.usos2.server.model.enumtype.UserRole instead
 */
@Deprecated(forRemoval = true)
public enum UserRole {
    STUDENT, LECTURER, ADMIN
}