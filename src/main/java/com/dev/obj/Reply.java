package com.dev.obj;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Reply {

	private String uniqueAddress;
	private String success;
	
	public String getUniqueAddress() {
		return uniqueAddress;
	}

	public void setUniqueAddress(String add) {
		this.uniqueAddress = add;
	}
	public Reply() {
		super();
	}
	public String getSuccess() {
		return success;
	}

	public void setSuccess(String succ) {
		this.success = succ;
	}

	}