package com.sky.opam.model;

public enum TranObjectType {
	/*server side*/
	Notification,  //N:******
	UserListRsp,  //UL:xu_li*XU Li:zhou_xia*ZHOU Xiandong
	UserOnLine,  //UON:xu_li*XU Li
	UserOutLine,  //UOUT:xu_li*XU Li
	MsgReceived,  //M:xu_li*XU Li:*******
	
	/*client side*/
	MsgToSend,  //zhou_xia*ZHOU Xiandong:******
	UserListRequire,
	ServiceStop,
}
