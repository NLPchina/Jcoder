package cn.com.infcn.api.test;

import com.alibaba.fastjson.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TestSocket {

	public static void main(String[] args) throws InterruptedException {
		//http://192.168.10.58:9095/api/LoginAction/longConn

//		byte[] bbbb = new byte[]{106, 115, 111, 110, 123, 34, 106, 115, 111, 110, 83, 116, 114, 34, 58, 116, 114, 117, 101, 44, 34, 109, 101, 115, 115, 97, 103, 101, 73, 100, 34, 58, 34, 49, 50, 51, 34, 44, 34, 109, 101, 116, 104, 111, 100, 78, 97, 109, 101, 34, 58, 34, 76, 111, 103, 105, 110, 65, 99, 116, 105, 111, 110, 34, 44, 34, 115, 121, 110, 34, 58, 116, 114, 117, 101, 44, 34, 99, 108, 97, 115, 115, 78, 97, 109, 101, 34, 58, 34, 76, 111, 103, 105, 110, 65, 99, 116, 105, 111, 110, 34, 44, 34, 97, 114, 103, 117, 109, 101, 110, 116, 115, 34, 58, 91, 34, 97, 97, 97, 45, 98, 98, 98, 34, 93, 125} ;
//		
//		System.out.println(new String(bbbb));
//		
//		if(1==1){
//			return ;
//		}

		Socket socket = new Socket();
		SocketAddress address = new InetSocketAddress("192.168.10.58", 9096);
		try {
			socket.connect(address, 60000);

			OutputStream outputStream = socket.getOutputStream();
			JSONObject job = new JSONObject();

			job.put("messageId", "123");
			job.put("className", "LoginAction");
			job.put("methodName", "login");
			job.put("arguments", new String[]{"aaa-bbb", "abc"});
			job.put("jsonStr", true);

			byte[] bytes = ("Json" + job.toJSONString()).getBytes("utf-8");

			outputStream.write(int2Byte(bytes.length + 4));
			outputStream.write(int2Byte(bytes.length));

			outputStream.write(bytes);
			outputStream.flush();


			Thread.sleep(1000L);

			InputStream inputStream = socket.getInputStream();


			DataInputStream dis = new DataInputStream(inputStream);

			dis.readInt();
			byte[] bs = new byte[dis.readInt()];

			dis.read(bs);

			System.out.println(new String(bs));


			System.out.println("连接成功！");
		} catch (IOException e) {
			System.out.println("连接超时！");
			e.printStackTrace();
		}
	}

	private static byte[] int2Byte(int v) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((v >>> 24) & 0xFF);
		bytes[1] = (byte) ((v >>> 16) & 0xFF);
		bytes[2] = (byte) ((v >>> 8) & 0xFF);
		bytes[3] = (byte) ((v >>> 0) & 0xFF);
		return bytes;
	}
}
