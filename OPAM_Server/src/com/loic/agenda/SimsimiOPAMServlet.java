package com.loic.agenda;

import java.io.IOException;

import javax.servlet.http.*;

import com.loic.agenda.outil.CopyOfSimsimiOutil;


@SuppressWarnings("serial")
public class SimsimiOPAMServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)throws IOException {
		String msg = req.getParameter("msg");
		
		resp.setContentType("text/xml;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Content-type","text/html;charset=UTF-8");
		
		CopyOfSimsimiOutil so = new CopyOfSimsimiOutil();
		resp.getWriter().println(so.getSimsimiRsp(msg));
	}
	
	
}
