package com.blork.localwall;

public class BadLocationException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4677984220236373166L;

	public BadLocationException(){ 
        super(); 
      } 
    public BadLocationException(String msg){ 
      super(msg); 
    } 

    public BadLocationException(String msg, Throwable t){ 
      super(msg,t);   
    } 
} 