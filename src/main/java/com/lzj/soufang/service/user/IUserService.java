package com.lzj.soufang.service.user;

import com.lzj.soufang.entity.User;
import com.lzj.soufang.service.ServiceResult;
import com.lzj.soufang.web.dto.UserDTO;

public interface IUserService {
    User findUserByName(String username);

    ServiceResult<UserDTO> findById(Integer userId);
}
