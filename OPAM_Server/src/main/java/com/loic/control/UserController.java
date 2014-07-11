package com.loic.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.loic.clientModel.StudentInfo;
import com.loic.dao.UserDAO;
import com.loic.model.User;

@Controller
@RequestMapping("/")
public class UserController {
	@Autowired
    private UserDAO userDAO;

    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody String greeting() {
    	return "BONJOUR";
    }
    
    @RequestMapping(value="/studentInfo", method=RequestMethod.POST)
    public @ResponseBody String getStudentInfo(@RequestParam(value = "name") String name) {
    	StudentInfo si = new StudentInfo();
    	User user = userDAO.findByName(name);
    	if(user != null){
    		si.login = user.getLogin();
    		si.name = user.getName();
    		si.email = user.getEmail();
    		si.school = user.getSchool();
    	}
    	return new Gson().toJson(si);
    }
}