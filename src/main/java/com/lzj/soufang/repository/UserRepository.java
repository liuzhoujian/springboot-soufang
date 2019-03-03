package com.lzj.soufang.repository;

import com.lzj.soufang.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 根据用户名查询用户
     * @param name
     * @return
     */
    User findUserByName(String name);
}
