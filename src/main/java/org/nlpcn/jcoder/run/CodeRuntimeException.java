package org.nlpcn.jcoder.run;

public class CodeRuntimeException extends RuntimeException {

	public CodeRuntimeException(String string) {
		super(string);
	}
	
	public CodeRuntimeException(Throwable e){
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
