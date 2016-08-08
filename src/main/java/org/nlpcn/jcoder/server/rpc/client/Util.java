package org.nlpcn.jcoder.server.rpc.client;

import org.jasypt.util.password.StrongPasswordEncryptor;

public class Util {

	private static final StrongPasswordEncryptor TEXT_ENCRYPTOR = new StrongPasswordEncryptor();

	private static final String PRIVATE_KEY = String.valueOf(System.currentTimeMillis());

	public static String encrypt(String text) {
		return TEXT_ENCRYPTOR.encryptPassword(PRIVATE_KEY + text);
	}

	public static boolean check(String text, String encrypt) {
		return TEXT_ENCRYPTOR.checkPassword(PRIVATE_KEY + text, encrypt);
	}

}
