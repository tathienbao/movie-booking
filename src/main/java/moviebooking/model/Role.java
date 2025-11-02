package moviebooking.model;

/**
 * User roles for role-based access control (RBAC).
 *
 * CUSTOMER: Regular users who can book movies and view their own bookings
 * ADMIN: Administrators who can manage movies and view all bookings
 */
public enum Role {
    CUSTOMER,
    ADMIN
}
