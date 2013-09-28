package com.loic.agenda;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.*;

import com.loic.agenda.model.Cours;
import com.loic.agenda.outil.Chiffrement;
import com.loic.agenda.outil.NewEssai;
import com.loic.agenda.outil.FailException;
import com.loic.agenda.outil.Java2xml;
import com.loic.agenda.outil.Essai;

@SuppressWarnings("serial")
public class AgendaOPAMServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {
		resp.setContentType("text/xml;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Content-type","text/html;charset=UTF-8");
		
		resp.getWriter().println("version 2.1");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String id = req.getParameter("username");
	    String mdp = req.getParameter("password");
	    
	    if(id.equals("") || mdp.equals("")){
	    	resp.getWriter().println("NO id or pw!");
	    }else {
	    	mdp = Chiffrement.decrypt(mdp, "OPAM");
	    	
	    	//long startTime=System.currentTimeMillis();
	    	
	    	NewEssai es = new NewEssai();
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
