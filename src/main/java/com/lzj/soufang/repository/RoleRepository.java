package com.lzj.soufang.repository;

import com.lzj.soufang.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * @param userId
     * @return
     */
    List<Role> findRolesByUserId(Integer userId);
}
