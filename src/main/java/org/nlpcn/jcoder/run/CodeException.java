package org.nlpcn.jcoder.run;

public class CodeException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public CodeException(String string) {
		super(string);
	}

	public CodeException(Exception e) {
		super(e);
	}

}
