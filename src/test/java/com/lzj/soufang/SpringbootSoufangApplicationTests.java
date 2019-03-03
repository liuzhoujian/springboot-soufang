package com.lzj.soufang;

import com.lzj.soufang.entity.User;
import com.lzj.soufang.repository.UserRepository;
import com.lzj.soufang.service.house.IQiNiuService;
import com.qiniu.common.QiniuException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootSoufangApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test01() {
        /*User user = userRepository.findById(1).get();
        System.out.println(user);*/

        /*User waliwali = userRepository.findUserByName("waliwali");
        System.out.println(waliwali);*/
    }

    @Test
    public void test02() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("admin"));

    }

    @Autowired
    private IQiNiuService qiNiuService;

    @Test
    public void test03() throws QiniuException {
        //通过key删除七牛云中的图片
        qiNiuService.delete("Flz6RxchaxDmQNjD233bqnvL3SOs");
    }
}

