package org.nlpcn.jcoder.server.rpc.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.run.mvc.processor.ApiActionInvoker;
import org.nlpcn.jcoder.run.mvc.processor.ApiMethodInvokeProcessor;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.server.rpc.client.RpcRequest;
import org.nlpcn.jcoder.server.rpc.client.RpcResponse;
import org.nlpcn.jcoder.server.rpc.client.Rpcs;
import org.nlpcn.jcoder.server.rpc.client.VFile;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.Testing;
import org.nutz.dao.Cnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * execute request
 * 
 * @author ansj
 *
 */
public class ExecuteHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(ExecuteHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		LOG.error(cause.getMessage());
		ctx.close();
		ChannelManager.remove(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		ChannelManager.remove(ctx.channel());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {

		String threadName = request.getClassName() + "@" + request.getMethodName() + "@RPC" + request.getMessageId() + "@" + DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");

		try {
			ThreadManager.add2ActionTask(threadName, Thread.currentThread());

			if (Testing.CODE_RUN.equals(request.getClassName())) { //执行远程方法
				try {
					executeCode(ctx, request, threadName);
				} catch (Exception e) {
					LOG.error(e.getMessage(),e);
					try {
						writeError(ctx, request, e.getMessage());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					throw e;
				}

			} else if (VFile.VFILE_LOCAL.equals(request.getClassName()) && VFile.VFILE_LOCAL.equals(request.getMethodName())) {
				write2Local(request);
			} else if (VFile.VFILE_CLIENT.equals(request.getClassName()) && VFile.VFILE_CLIENT.equals(request.getMethodName())) {
				clientRead(request);
			} else if (VFile.VFILE_SERVER.equals(request.getClassName()) && VFile.VFILE_SERVER.equals(request.getMethodName())) {
				serverRead(ctx, request);
			} else {
				try {
					executeTask(ctx, request, threadName);
				} catch (Exception e) {
					LOG.error(e.getMessage(),e);
					try {
						writeError(ctx, request, e.getMessage());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					throw e;
				}
			}
		} finally {
			ThreadManager.removeActionIfOver(threadName);
		}
	}

	/**
	 * 写入文件到服务器端
	 * 
	 * @param request
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void write2Local(RpcRequest request) throws FileNotFoundException, IOException {
		byte[] data = (byte[]) request.getArguments()[0];
		String messageId = request.getMessageId();
		File file = new File(StaticValue.UPLOAD_DIR, messageId + ".temp");
		if (data.length > 4) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(file, file.exists())) {
				fileOutputStream.write(data, 4, data.length);
			}
		} else {
			String filePath = file.getAbsolutePath();
			file.renameTo(new File(filePath.substring(0, filePath.length() - 5)));
		}
	}

	/**
	 * 写错误流到服务器端
	 * 
	 * @param ctx
	 * @param request
	 * @param message
	 */
	private void writeError(ChannelHandlerContext ctx, RpcRequest request, String message) {
		RpcResponse response = Rpcs.getRep();
		if (response == null) {
			response = new RpcResponse(request.getMessageId());
		}
		response.setError("server has err : " + message);
		ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	/**
	 * 从服务器端读取流到客户端
	 * 
	 * @param request
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void serverRead(ChannelHandlerContext ctx, RpcRequest request) throws FileNotFoundException, IOException {
		VFile file = (VFile) request.getArguments()[0];

		if (file == null || !file.check()) {
			writeError(ctx, request, "verification err");
			return;
		}

		if (file.isFile()) {
			writeFromFile(ctx, request, file);
		} else {
			writeFromStream(ctx, request, file);
		}
	}

	/**
	 * 内存中的流
	 * 
	 * @param ctx
	 * @param request
	 * @param file
	 */
	private void writeFromStream(ChannelHandlerContext ctx, RpcRequest request, VFile file) {
		byte[] bytes = null;

		try {
			byte[] b = new byte[file.getLen()];

			InputStream inputStream = VFile.STREAM_MAP.get(file.getId());

			int len = inputStream.read(b);

			if (len <= 0) {
				bytes = null;
				InputStream remove = VFile.STREAM_MAP.remove(file.getId());
				if (remove != null) {
					remove.close();
				}
			} else if (len == b.length) {
				bytes = b;
			} else {
				bytes = Arrays.copyOfRange(b, 0, len);
			}
			if (bytes != null) {
				file.setOff(file.getOff() + bytes.length);
			}
			Rpcs.getRep().setResult(bytes);
			ctx.channel().writeAndFlush(Rpcs.getRep()).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

		} catch (Exception e) {
			try {
				writeError(ctx, request, e.getMessage());
			} finally {
				InputStream remove = VFile.STREAM_MAP.remove(file.getId());
				if (remove != null) {
					try {
						remove.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}

		}
	}

	/**
	 * 文件流
	 * 
	 * @param ctx
	 * @param request
	 * @param file
	 * @throws IOException
	 */
	private void writeFromFile(ChannelHandlerContext ctx, RpcRequest request, VFile file) throws IOException {
		if (VFile.STREAM_MAP.contains(file.getId())) {
			writeFromStream(ctx, request, file);
		} else {
			FileInputStream fileInputStream = new FileInputStream(new File(file.getClientLocalPath()));
			fileInputStream.skip(file.getOff());
			VFile.STREAM_MAP.put(file.getId(), fileInputStream);
			writeFromStream(ctx, request, file);
		}
	}

	/**
	 * 客户端读取流到服务器端
	 * 
	 * @param request
	 * @throws IOException
	 * @throws Exception
	 */
	private void clientRead(RpcRequest request) throws IOException, Exception {
		VFile vfile = (VFile) VFile.BUFFERED_MAP.remove(request.getMessageId());
		if (vfile == null) {
			throw new IOException("vfile form bufferd map is null");
		}
		try {
			Object result = request.getArguments()[0];

			if (result == null) {
				vfile.addBytes(VFile.END_BYTE);
			} else if (result instanceof byte[]) {
				vfile.addBytes((byte[]) result);
			} else {
				throw new IOException(result.toString());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
			vfile.addBytes(VFile.ERR_BYTE);
			throw e;
		}
	}

	/**
	 * 具体的执行一个task
	 * 
	 * @param ctx
	 * @param request
	 * @param threadName
	 */
	private void executeCode(ChannelHandlerContext ctx, RpcRequest request, String threadName) {

		RpcResponse response = Rpcs.getRep();

		try {

			Object[] arguments = request.getArguments();

			@SuppressWarnings("all")
			HashMap<String, String> map = (HashMap<String, String>) arguments[arguments.length - 1];

			//检查用户名密码是否正确
			User user = StaticValue.systemDao.findByCondition(User.class, Cnd.where("name", "=", map.get("name")).and("password", "=", map.get("password")));

			if (user == null) {
				throw new ApiException(ApiException.Unauthorized, "you name or password err!");
			}

			String code = map.get("code"); //获得code

			Object[] param = new Object[arguments.length - 1];

			for (int i = 0; i < param.length - 1; i++) {
				param[i] = arguments[i];
			}

			Task task = new Task();

			task.setCode(code);

			JavaRunner runner = new JavaRunner(task).compile().instance();

			ExecuteMethod method = task.codeInfo().getExecuteMethod(request.getMethodName());

			if (method == null) {
				throw new ApiException(404, "not find api " + task.codeInfo().getClassz().getName() + " by method name " + request.getMethodName() + " in mapping");
			}

			Object result = runner.execute(method.getMethod(), param);

			response.setResult(result);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
			response.setError("server err :" + e.getMessage());
		}
		ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	/**
	 * 具体的执行一个task
	 * 
	 * @param ctx
	 * @param request
	 * @param threadName
	 */
	private void executeTask(ChannelHandlerContext ctx, RpcRequest request, String threadName) {

		RpcResponse response = Rpcs.getRep();

		try {

			ApiActionInvoker invoker = StaticValue.MAPPING.getOrCreateByUrl(request.getClassName(), request.getMethodName());

			if (invoker == null) {
				throw new ApiException(404, "not find api in mapping");
			}

			Task task = TaskService.findTaskByCache(request.getClassName());

			if (task == null) {
				throw new ApiException(404, "not find api by name " + request.getClassName() + " in mapping");
			}

			ExecuteMethod method = task.codeInfo().getExecuteMethod(request.getMethodName());

			if (method == null) {
				throw new ApiException(404, "not find api " + request.getClassName() + " by method name +" + request.getMethodName() + "+ in mapping");
			}

			ApiMethodInvokeProcessor invokeProcessor = invoker.getChain().getInvokeProcessor();

			if (method.isRpc()) {
				Object result = invokeProcessor.executeByCache(task, method.getMethod(), request.getArguments());
				if (request.isJsonStr()) {
					response.setResult(JSON.toJSONString(result));
				} else {
					response.setResult(result);
				}

			} else {
				response.setError("server err : request " + request.getClassName() + "/" + request.getMethodName() + " not a rpc api");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
			response.setError("server err :" + e.getMessage());
		}
		ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}
}
