package com.loic.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.loic.dao.UserDAO;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserDAO userDAO;

    @RequestMapping(value="/", method=RequestMethod.GET)
    public @ResponseBody String greeting() {
//        List<User> users = userDAO.findByName("lili");
//        return new Gson().toJson(users);
    	return "HELLO !@";
    }
}