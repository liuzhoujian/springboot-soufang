package com.lzj.soufang.service.user;

import com.lzj.soufang.entity.Role;
import com.lzj.soufang.entity.User;
import com.lzj.soufang.repository.RoleRepository;
import com.lzj.soufang.repository.UserRepository;
import com.lzj.soufang.service.ServiceResult;
import com.lzj.soufang.web.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ServiceResult<UserDTO> findById(Integer userId) {
        User user = userRepository.findById(userId).get();
        if (user == null) {
            return ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }

    @Override
    public User findUserByName(String username) {
        //根据用户名查询用户
        User user = userRepository.findUserByName(username);
        if(user == null) {
            return null;
        }

        //通过用户id查询对应的角色
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if(roles == null || roles.isEmpty()) {
            throw new DisabledException("非法权限");
        }

        List<GrantedAuthority> authorityList = new ArrayList<>();

        //将查询到的权限封装到user中
        roles.forEach(role -> authorityList.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));

        user.setAuthorityList(authorityList);

        return user;
    }
}
