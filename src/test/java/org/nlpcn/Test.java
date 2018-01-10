package org.nlpcn;

/**
 * Created by Ansj on 10/01/2018.
 */
public class Test {

	public static void main(String[] args) {
		try {
			throw new Exception("aaa") ;
		}catch (Exception e){
			throw new RuntimeException(e) ;
		}finally {
			System.out.println("cccccccccc");
		}
	}
}
