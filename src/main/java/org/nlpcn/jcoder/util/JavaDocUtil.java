package org.nlpcn.jcoder.util;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.ClassDoc;
import org.nlpcn.jcoder.domain.ClassDoc.MethodDoc;
import org.nlpcn.jcoder.domain.ClassDoc.MethodDoc.ParamDoc;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

/**
 * 根据java文件解析
 * 
 * @author ansj
 *
 */
public class JavaDocUtil {

	public static ClassDoc parse(Reader reader) throws Exception {

		CompilationUnit cu = JavaParser.parse(reader, true);

		List<TypeDeclaration> types = cu.getTypes();

		if (types.size() == 0 || !(types.get(0) instanceof ClassOrInterfaceDeclaration)) {
			return null;
		}

		ClassOrInterfaceDeclaration cla = (ClassOrInterfaceDeclaration) types.get(0);

		ClassDoc cd = new ClassDoc(cla.getName());

		if (cla.getComment() != null)
			parseContent(cd, cla.getComment().getContent());

		for (Node node : cla.getChildrenNodes()) {

			if (node instanceof SingleMemberAnnotationExpr) {

				SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) node;

				if (sma.getName().getName().equals("Single")) {
					if ("false".equals(sma.getMemberValue().toString())) {
						cd.setSingle(false);
					}
				}

			} else if (node instanceof MethodDeclaration) {
				explainMethod(cd, (MethodDeclaration) node);
			}
		}

		return cd;

	}

	private static void explainMethod(ClassDoc cd, MethodDeclaration node) {

		MethodDeclaration method = (MethodDeclaration) node;

		List<AnnotationExpr> annotations = method.getAnnotations();

		boolean flag = false;

		boolean defaultExecute = false;

		for (AnnotationExpr an : annotations) {
			String name = an.getName().getName();
			if (name.equals("Execute")) {
				flag = true;
				break;
			} else if (name.equals("DefaultExecute")) {
				flag = true;
				defaultExecute = true;
				break;
			}
		}

		if (!flag) {
			return;
		}

		MethodDoc md = (MethodDoc) cd.createSubDoc(method.getName());

		md.setDefaultExecute(defaultExecute);

		Map<String, String> paramMap = null;
		if (method.getComment() != null) {
			paramMap = parseContent(md, method.getComment().getContent());
		} else {
			paramMap = new HashMap<>();
		}

		List<Parameter> parameters = method.getParameters();

		for (Parameter param : parameters) {
			List<AnnotationExpr> ans = param.getAnnotations();
			String name = null;
			String fieldName = null;
			for (AnnotationExpr annotationExpr : ans) {
				if (annotationExpr.getName().getName().equals("Param")) {
					annotationExpr.getChildrenNodes().remove(0);
					for (Node tempNode : annotationExpr.getChildrenNodes()) {
						String str = tempNode.toString();

						if (str.contains("=")) {
							if (str.startsWith("value")) {
								name = str.toString().replaceFirst("value", "").replaceFirst("=", "").replace("\"", "").trim();
								break;
							}
						} else {
							name = str.toString().replace("\"", "").trim();
						}

					}
					break;
				}
			}

			fieldName = param.getId().getName();

			if (StringUtil.isBlank(name)) {
				name = fieldName;
			}

			ParamDoc pd = (ParamDoc) md.createSubDoc(name);

			pd.setFieldName(fieldName);

			pd.setType(param.getType().toString());

			String content = paramMap.get(fieldName);

			if (content == null) {
				content = paramMap.get(name);
			}

			pd.setContent(content);

		}

	}

	/**
	 * 解析class上面的注释
	 * 
	 * @param cd
	 * @param content
	 */
	private static void parseContent(ClassDoc cd, String content) {
		List<String> lines = makeContentToLines(content);
		for (String string : lines) {
			if (string.charAt(0) != '@') {
				cd.setContent(string);
			} else {
				Matcher matcher = Pattern.compile("\\s+").matcher(string);
				if (matcher.find()) {
					cd.addAttr(string.substring(0, matcher.start()), string.substring(matcher.end(), string.length()));
				}
			}
		}
	}

	/**
	 * 解析方法上面的注释
	 * 
	 * @param md
	 * @param content
	 * @return
	 */
	private static Map<String, String> parseContent(MethodDoc md, String content) {
		List<String> lines = makeContentToLines(content);

		Map<String, String> paramMap = new HashMap<>();

		for (String string : lines) {
			if (string.charAt(0) != '@') {
				md.setContent(string);
			} else if (string.startsWith("@param")) {
				String trim = string.replaceFirst("@param", "").trim();
				Matcher matcher = Pattern.compile("\\s+").matcher(trim);

				if (matcher.find()) {
					paramMap.put(trim.substring(0, matcher.start()), trim.substring(matcher.end(), trim.length()));
				}

			} else if (string.startsWith("@return")) {
				md.setRetrunContent(string.replaceFirst("@return", ""));
			} else {
				Matcher matcher = Pattern.compile("\\s+").matcher(string);
				if (matcher.find()) {
					md.addAttr(string.substring(0, matcher.start()), string.substring(matcher.end(), string.length()));
				}
			}
		}

		return paramMap;
	}

	private static List<String> makeContentToLines(String content) {
		String[] split = content.split("\n");

		List<String> lines = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		for (String string : split) {
			String str = string.replaceAll("^\\s+\\*", "").trim();

			if (StringUtil.isBlank(str)) {
				continue;
			}

			if (str.startsWith("@")) {
				String line = sb.toString().trim();
				if (StringUtil.isNotBlank(line)) {
					lines.add(line);
				}
				sb = new StringBuilder();
			}
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(str);
		}

		String line = sb.toString().trim();

		if (StringUtil.isNotBlank(line)) {
			lines.add(line);
		}

		return lines;
	}

}
