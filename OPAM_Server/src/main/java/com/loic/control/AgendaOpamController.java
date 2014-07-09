package com.loic.control;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.loic.clientModel.ClassInfoClient;
import com.loic.clientModel.UserClassPackage;
import com.loic.clientModel.UserClient;
import com.loic.dao.UserDAO;
import com.loic.util.Chiffrement;
import com.loic.util.NetAntMutiThreadsGSON;


@Controller
@RequestMapping("/agendaopam")
public class AgendaOpamController {

    @Autowired
    private UserDAO userDAO;

    @RequestMapping(value="/", method=RequestMethod.GET)
    public @ResponseBody String welcomeAgend() {
    	String s = "<form action=\"/agendaopam\" method=\"post\"><input id=\"username\" name=\"username\" type=\"text\" value=\"\" size=\"25\" autocomplete=\"false\"/><input id=\"password\" name=\"password\" type=\"password\" value=\"\" size=\"25\" autocomplete=\"off\"/><input type=\"submit\" value=\"OK\" /></form>";
    	return "INTagenda version 3.0 </br> "+s;
    }
    
    @RequestMapping(value="/", method=RequestMethod.POST)
    public @ResponseBody String getAgenda(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {
    	UserClassPackage mypackage = new UserClassPackage();
    	if(username.equals("") || password.equals("")){
    		mypackage.addClassInfo(ErrorPackToClassInfo("NO id or passeword !"));
	    }else {
  	    	password = Chiffrement.decrypt(password, "OPAM");    	
	    	if(password==null){
	    		mypackage.addClassInfo(ErrorPackToClassInfo("Password NOT well encrypted!"));
	    	}else {
	    		NetAntMutiThreadsGSON es = new NetAntMutiThreadsGSON();
	    		
	    		mypackage.setClassInfos(es.start(username, password));
	    	    mypackage.getUser().setName(es.userName);
	    	    mypackage.getUser().setLogin(username);
	    	    mypackage.getUser().setNumWeekUpdated(getNumWeek());
			}
		}
    	Gson gson = new Gson();
		return gson.toJson(mypackage);
    }
    
    private int getNumWeek(){		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		int xq = c.get(Calendar.DAY_OF_WEEK);
		if(xq==1){
			c.set(Calendar.DATE, c.get(Calendar.DATE)-1);
		}
		SimpleDateFormat sdf = new  SimpleDateFormat();
		sdf.applyPattern("w");
		return Integer.parseInt(sdf.format(c.getTime()));
	}
    
    private ClassInfoClient ErrorPackToClassInfo(String msg){
    	ClassInfoClient c = new ClassInfoClient("E", "E");
    	c.name="FailException";
		c.students = msg;
		return c;
	}
}