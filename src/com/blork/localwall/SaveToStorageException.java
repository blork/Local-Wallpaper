package com.blork.localwall;

public class SaveToStorageException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6891797972730795760L;

	public SaveToStorageException(){ 
        super(); 
      } 
    public SaveToStorageException(String msg){ 
      super(msg); 
    } 

    public SaveToStorageException(String msg, Throwable t){ 
      super(msg,t);   
    } 
} 