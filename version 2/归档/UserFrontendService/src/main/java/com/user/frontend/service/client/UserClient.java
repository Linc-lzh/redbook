package com.user.frontend.service.client;

import com.user.frontend.service.dto.LoginRequestDTO;
import com.user.frontend.service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "user-service")
public interface UserClient {

    @RequestMapping(method = RequestMethod.POST, value = "/user/service/endpoint/login")
    UserDTO login(@RequestBody LoginRequestDTO loginRequest);

    @RequestMapping(method = RequestMethod.POST, value = "/user/service/endpoint/register")
    boolean register(UserDTO user);
}

