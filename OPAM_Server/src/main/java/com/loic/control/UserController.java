package com.loic.control;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.loic.dao.UserDAO;
import com.loic.model.User;

@Controller
@RequestMapping("/")
public class UserController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    @Autowired
    private UserDAO userDAO;

    @RequestMapping(value="/", method=RequestMethod.GET)
    public @ResponseBody String greeting() {
//        List<User> users = userDAO.findByName("lili");
//        return new Gson().toJson(users);
    	return "HELLO !@";
    }
}