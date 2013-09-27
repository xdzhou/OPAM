//package com.loic.agenda.outil;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.gargoylesoftware.htmlunit.BrowserVersion;
//import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
//import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
//import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;
//
//
//public class SimOutil {
//	private static SimOutil sOutil;
//	private HtmlPage page;
//	WebClient webClient;
//	List<String> collectedAlerts;
//	
//	public SimOutil(){
//		webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
//		webClient.getCookieManager().setCookiesEnabled(true);
//		webClient.setThrowExceptionOnScriptError(false);
//		webClient.setCssEnabled(false);
//	    webClient.setThrowExceptionOnFailingStatusCode(false);
//		webClient.setTimeout(10*1000);
//		
//		collectedAlerts = new ArrayList<String>();
//	    webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
//		
//		try {
//			page = webClient.getPage("http://www.simsimi.com/talk.htm");
//		} catch (FailingHttpStatusCodeException e) {
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		page.executeJavaScript("$(\"#agreement\").hide();");
//		page.executeJavaScript("$(\"#wrapper-talk #contents\").show();");
//	}
//	
//	public static SimOutil getInstance(){
//		if(sOutil==null){
//			sOutil = new SimOutil();
//		}
//		return sOutil;
//	}
//	
//	public String GetSimsimi(String msg){
//		GetSimsimiAPI(msg);
//		
//		webClient.waitForBackgroundJavaScript(3000);  
//		webClient.setAjaxController(new NicelyResynchronizingAjaxController()); 
//
//		//webClient.closeAllWindows();
//		String s = new String(collectedAlerts.get(0));
//		collectedAlerts.clear();
//		return s;
//	}
//	
//	private void GetSimsimiAPI(String question){
//		String js = "function MySendChat(inputMsg){var msg = encodeURIComponent(inputMsg);var res = \"\";$.ajax({url: \"./func/req\",data: {msg:msg, lc:$(\"#hid-lang\").val()},type: 'GET',dataType: 'json',contentType: \"application/json; charset=utf-8\",error: function(xhr, textStatus, errorThrown){alert(\"error!\");complete();},success: function(json) {try{if(json.result == \"100\"){res = json.response;alert(res);}else{res = \"I have no response.\";	alert(res);}$(\".templete\").clone().removeClass(\"hidden\").removeClass(\"templete\").addClass(\"new-templete\").appendTo(\"#msgs\");	$(\".new-templete:last\").find(\".right-convo\").html(\"\").append(\"<span>\"+inputMsg.htmlEntities()+\"</span>\");$(\".new-templete:last\").find(\".left-convo\").html(\"\").append(\"<span>\"+getAdResponse(res.htmlEntities())+\"</span>\");complete();}catch(e){alert(\"error!\");complete();}}});}  MySendChat('"+question+"');";		
//		page.executeJavaScript(js);
//	}
//
//}
