package org.nlpcn.jcoder.util;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.CodeRuntimeException;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.run.java.JavaRunner;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.google.common.base.Joiner;

/**
 * 根据java文件解析
 * 
 * @author ansj
 *
 */
public class JavaSource2RpcUtil {

	private static final String CONSTRUCOTR_CODE = "	public [CLASS_NAME]() {\n" + "	\n" + "	}\n" + "\n" + "	public [CLASS_NAME](boolean syn, long timeout) {\n"
			+ "		this.__JCODER__syn = syn;\n" + "		this.__JCODER__timeout = timeout;\n" + "	}\n" + "\n" + "	private boolean __JCODER__syn = true;\n" + "\n"
			+ "	private long __JCODER__timeout = 1200000L;\n" + "\n" + "	public void set__JCODER__syn(boolean syn) {\n" + "		this.__JCODER__syn = syn;\n" + "	}\n" + "\n"
			+ "	public void set__JCODER__timeout(long timeout) {\n" + "		this.__JCODER__timeout = timeout;\n" + "	}\n" + "\n";

	public static void main(String[] args) {
		System.out.println(METHOD_TEMPLATE);
	}

	public static String makeRpcSource(Task task) throws Exception {
		if (task.codeInfo().getClassz() == null) {
			new JavaRunner(task).compile();
		}

		if (task.codeInfo().getClassz() == null) {
			throw new CodeRuntimeException(task.getName() + " can make to class err!");
		}

		return makeRpcSource(new StringReader(task.getCode()), task.codeInfo().getClassz());
	}

	/**
	 * make task sourced to rpc source
	 * 
	 * @param reader
	 * @param classz
	 * @return
	 * @throws Exception
	 */
	public static String makeRpcSource(Reader reader, Class<?> classz) throws Exception {

		List<Method> methods = findAllRpcMthod(classz.getMethods());

		if (methods == null || methods.size() == 0) {
			return null;
		}

		Set<String> methodNames = methods.stream().map(m -> m.getName()).collect(Collectors.toSet());

		StringBuilder sb = new StringBuilder();

		CompilationUnit cu = JavaParser.parse(reader, true);

		PackageDeclaration package1 = cu.getPackage();

		sb.append(package1.toString());
		for (ImportDeclaration importDeclaration : cu.getImports()) {
			sb.append(importDeclaration);
		}

		ClassOrInterfaceDeclaration cla = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);

		sb.append("public class " + cla.getName() + " {\n");

		sb.append(CONSTRUCOTR_CODE.replace("[CLASS_NAME]", cla.getName()));

		for (Node node : cla.getChildrenNodes()) {
			if (node instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) node;
				if (methodNames.contains(method.getName())) {
					sb.append(explainMethod(method));
					sb.append("\n");
				}
			}
		}
		sb.append("\n}");

		return sb.toString();
	}

	private static List<Method> findAllRpcMthod(Method[] methods) throws ClassNotFoundException {

		List<Method> rpcMethod = new ArrayList<>();

		for (Method method : methods) {
			Execute e = method.getAnnotation(Execute.class);
			DefaultExecute de = method.getAnnotation(DefaultExecute.class);

			if (e != null && e.rpc()) {
				rpcMethod.add(method);
			} else if (de != null && de.rpc()) {
				rpcMethod.add(method);
			}
		}

		return rpcMethod;
	}

	private static final String METHOD_TEMPLATE = "	public [RETURN] [METHOD_NAME]([ARGS]) throws Throwable {\n"
			+ "		return ([RETURN]) org.nlpcn.jcoder.server.rpc.client.RpcClient.getInstance().proxy(new org.nlpcn.jcoder.server.rpc.client.RpcRequest(java.util.UUID.randomUUID().toString(), this.getClass().getSimpleName(),\n"
			+ "				\"[METHOD_NAME]\", __JCODER__syn, false, __JCODER__timeout, new Object[] { [ARGS_NAME] }));\n" + "	}\n" + "	\n"
			+ "	public String [METHOD_NAME]__jsonStr([ARGS]) throws Throwable {\n"
			+ "		return (String) org.nlpcn.jcoder.server.rpc.client.RpcClient.getInstance().proxy(new org.nlpcn.jcoder.server.rpc.client.RpcRequest(java.util.UUID.randomUUID().toString(), this.getClass().getSimpleName(),\n"
			+ "				\"[METHOD_NAME]\", __JCODER__syn, true, __JCODER__timeout, new Object[] { [ARGS_NAME] }));\n" + "	}\n";

	private static String explainMethod(MethodDeclaration method) {

		String methodCode = METHOD_TEMPLATE;

		List<Parameter> parameters = method.getParameters();

		String args = "";
		String argsName = "";

		if (parameters != null && parameters.size() > 0) {
			args = Joiner.on(" , ").join(parameters);
			argsName = Joiner.on(" , ").join(parameters.stream().map(p -> p.getId()).collect(Collectors.toList()));
		}

		String returnType = baseConver(method.getType().toString());

		if ("void".equals(returnType)) {
			methodCode = methodCode.replace("return ([RETURN]) ", "");
		}

		methodCode = methodCode.replace("[RETURN]", returnType);

		return methodCode.replace("[METHOD_NAME]", method.getName()).replace("[ARGS]", args).replace("[ARGS_NAME]", argsName);

	}

	/**
	 * 防止obj到基本数据类型转换
	 * 
	 * @param string
	 * @return
	 */
	private static String baseConver(String type) {
		switch (type) {
		case "byte":
			return "Byte";
		case "short":
			return "Short";
		case "int":
			return "Integer";
		case "long":
			return "Long";
		case "float":
			return "Float";
		case "double":
			return "Double";
		case "char":
			return "Character";
		case "boolean":
			return "Boolean";
		default:
			return type;
		}

	}

}
