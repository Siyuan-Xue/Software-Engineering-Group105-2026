package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository contract for account lookup and activation state.
 */
public interface UserRepository extends CrudRepository<User> {
    Optional<User> findByEmail(String email);

    List<User> listByRole(UserRole role);

    List<User> listActive();

    boolean setActive(UUID userId, boolean active);
}
