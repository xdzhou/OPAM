package com.loic.agenda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.*;

import com.loic.agenda.model.Cours;
import com.loic.agenda.outil.Chiffrement;
import com.loic.agenda.outil.NetAnt;
import com.loic.agenda.outil.Java2xml;

@SuppressWarnings("serial")
public class AgendaOPAMServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {
		resp.setContentType("text/xml;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Content-type","text/html;charset=UTF-8");
		
		resp.getWriter().println("INTagenda version 2.3");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String id = req.getParameter("username");
	    String mdp = req.getParameter("password");
	    
	    if(id.equals("") || mdp.equals("")){
	    	resp.getWriter().println(ServerError("NO id or passeword!"));
	    }else {
	    	mdp = Chiffrement.decrypt(mdp, "OPAM");
	    	
	    	if(mdp==null) resp.getWriter().println(ServerError("Password NOT well encrypted!"));
	    	else {
	    		NetAnt es = new NetAnt();
			    List<Cours> cours = es.start(id, mdp);
			    String userName = es.userName;
			    Java2xml b = new Java2xml(cours, userName);
			    
			    //long endTime=System.currentTimeMillis();
			    //System.out.println("runing time: "+(endTime-startTime)+"ms");
				
				resp.setContentType("text/xml;charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				resp.setHeader("Content-type","text/html;charset=UTF-8");
				resp.getWriter().println(b.getXML());
			}
		}
	}
	
	private String ServerError(String msg) throws IOException{
		List<Cours> cours = new ArrayList<Cours>();
		Cours c = new Cours("E", "E");
		c.name="FailException";
		c.type=msg;
		c.position = "49_1";
		cours.add(c);
		Java2xml b = new Java2xml(cours, "M. X");
		return b.getXML();
	}
}
