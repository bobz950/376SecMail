package edu.depaul.secmail;

import java.util.ArrayList;

public interface Content {
	
	public void setContent(String s);
	public void addContent(String s);
	public String display();
	public void addToResponseHeader(String s);
	public ArrayList<String> getAddedReponseHeaders();
}
