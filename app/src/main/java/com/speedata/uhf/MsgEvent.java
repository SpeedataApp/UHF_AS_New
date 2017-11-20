package com.speedata.uhf;


public class MsgEvent {
	private String type;
	private Object data;

	public MsgEvent(String type, Object data) {
		super();
		this.data = data;
		this.type = type;
	}

	public Object getMsg() {
		return data;
	}

	public String getType() {
		return type;
	}

}
