package com.loic.control;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import com.loic.dao.UserDAO;
import com.loic.model.User;
import com.loic.util.Chiffrement;
import com.loic.util.NetAntMutiThreadsGSON;
import com.loic.util.TaskAgendaLoad;

@Controller
@RequestMapping("/agendaopam")
public class AgendaOpamController {
    @Autowired
    private UserDAO userDAO;

    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody String welcomeAgend() {
    	//TODO
    	String s = "<form action=\"/OPAM_Server-0.1.0/agendaopam\" method=\"post\"><input id=\"username\" name=\"username\" type=\"text\" value=\"\" size=\"25\" autocomplete=\"false\"/><input id=\"password\" name=\"password\" type=\"password\" value=\"\" size=\"25\" autocomplete=\"off\"/><input type=\"submit\" value=\"OK\" /></form>";
    	return "INTagenda version 3.0 </br> "+s;
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public @ResponseBody String getAgenda(@RequestParam(value = "username") String login, @RequestParam(value = "password") String psw) {
    	UserClassPackage mypackage = new UserClassPackage();
    	if(login.equals("") || psw.equals("")){
    		mypackage.addClassInfo(ErrorPackToClassInfo("NO id or passeword !"));
	    }else {
  	    	String password = Chiffrement.decrypt(psw, "OPAM");    	
	    	if(password==null){
	    		//mypackage.addClassInfo(ErrorPackToClassInfo("Password NOT well encrypted!"));
	    		//TODO
	    		password = psw;
	    	}
	    		//NetAntMutiThreadsGSON es = new NetAntMutiThreadsGSON(); 
	    		TaskAgendaLoad es = new TaskAgendaLoad(userDAO);
	    		mypackage.setClassInfos(es.start(login, password));
	    		String userName = es.getUserName();
	    	    mypackage.getUser().setName(userName);
	    	    mypackage.getUser().setLogin(login);
	    	    mypackage.getUser().setNumWeekUpdated(getNumWeek());
	    	    
	    	    User user = userDAO.findByLogin(login);
	    	    if(user == null && !userName.equals("")){
	    	    	user = new User();
	    	    	user.setLogin(login);
		    	    user.setName(userName);
		    	    user.setNumWeekUpdated(getNumWeek());
		    	    userDAO.save(user);
	    	    }else {
					user.setNumWeekUpdated(getNumWeek());
					userDAO.update(user);
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