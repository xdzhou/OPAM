package com.loic.control;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.APIConnectionException;
import cn.jpush.api.common.APIRequestException;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

import com.loic.config.Config;
import com.loic.dao.MessageDAO;
import com.loic.dao.UserDAO;
import com.loic.model.Message;
import com.loic.model.User;

@Controller
@RequestMapping("/talk")
public class MessageController {
	private static final JPushClient jpushClient = new JPushClient(Config.JPUSH_MASTER_SECRET,Config.JPUSH_APPKEY);
	@Autowired
    private UserDAO userDAO;
	@Autowired
    private MessageDAO messageDAO;
	
    @RequestMapping(value="/send", method=RequestMethod.POST)
    public @ResponseBody String sendMessage(@RequestParam(value = "sender") String sender,
    		@RequestParam(value = "recevier") String recevier,
    		@RequestParam(value = "message") String msg) {
    	User sender_user = userDAO.findByLogin(sender);
    	if(sender_user == null) return "NO USER: "+sender;
    	User recevier_user = userDAO.findByLogin(recevier);
    	if(recevier_user == null) return "NO USER: "+recevier;
    	Message message = new Message();
    	message.setSender(sender_user);
    	message.setRecevier(recevier_user);
    	message.setTime(new Date());
    	message.setContent(msg);
    	messageDAO.save(message);
    	
    	PushPayload.Builder payload = PushPayload.newBuilder().setPlatform(Platform.android());
    	payload.setAudience(Audience.alias(recevier));
    	payload.setNotification(Notification.android("Alert", sender_user.getName()+" send you a msg", null));
    	payload.setMessage(cn.jpush.api.push.model.Message.newBuilder()
                .setMsgContent(msg)
                .setTitle(sender_user.getName())
                .build());
    	try {
			jpushClient.sendPush(payload.build());
		} catch (APIConnectionException e) {
			try {
				jpushClient.sendPush(payload.build());
			} catch (Exception e1) {
				return e1.getMessage();
			} 
		} catch (APIRequestException e) {
			return e.getMessage();
		}
    	return "OK";
    }

}