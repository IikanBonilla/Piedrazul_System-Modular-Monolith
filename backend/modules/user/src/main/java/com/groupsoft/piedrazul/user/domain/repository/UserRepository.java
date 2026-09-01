package com.groupsoft.piedrazul.user.domain.repository;

import com.groupsoft.piedrazul.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
