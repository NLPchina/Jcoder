import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.ClassDoc;
import org.nlpcn.jcoder.domain.ClassDoc.MethodDoc;
import org.nlpcn.jcoder.domain.ClassDoc.MethodDoc.ParamDoc;

import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

public class DocletTest {

	public static void main(String[] args) throws Exception {

		// creates an input stream for the file to be parsed

		String content = IOUtil.getContent(new File("C:\\Users\\ansj\\Downloads\\jdcoder_sdk_20160702110233\\jcoder_sdk\\src\\org\\nlpcn\\ansj\\api\\SegApi.java"), "utf-8");

		try (StringReader stringReader = new StringReader(content)) {

			CompilationUnit cu = JavaParser.parse(stringReader, true);

			List<TypeDeclaration> types = cu.getTypes();

			ClassOrInterfaceDeclaration cla = (ClassOrInterfaceDeclaration) types.get(0);

			ClassDoc cd = new ClassDoc(cla.getName());

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

			System.out.println(JSONObject.toJSON(cd));
		}

	}

	private static void explainMethod(ClassDoc cd, MethodDeclaration node) {

		MethodDeclaration method = (MethodDeclaration) node;

		List<AnnotationExpr> annotations = method.getAnnotations();

		boolean flag = false;

		for (AnnotationExpr an : annotations) {
			String name = an.getName().getName();
			if (name.equals("Execute") || name.equals("DefaultExecute")) {
				flag = true;
				break;
			}
		}

		if (!flag) {
			return;
		}

		MethodDoc md = (MethodDoc) cd.createSubDoc(method.getName());

		Map<String, String> paramMap = parseContent(md, method.getComment().getContent());

		List<Parameter> parameters = method.getParameters();

		for (Parameter param : parameters) {
			List<AnnotationExpr> ans = param.getAnnotations();
			String name = null;
			String fieldName = null;
			for (AnnotationExpr annotationExpr : ans) {
				if (annotationExpr.getName().getName().equals("Param")) {
					name = annotationExpr.getChildrenNodes().get(1).toString().replace("\"", "");
					break;
				}
			}

			fieldName = param.getId().getName();

			if (StringUtil.isBlank(name)) {
				name = fieldName;
			}

			ParamDoc pd = (ParamDoc) md.createSubDoc(name);

			pd.setFieldName(fieldName);

			String content = paramMap.get(fieldName);

			if (content == null) {
				content = paramMap.get(name);
			}

			pd.setContent(content);

		}

	}

	/**
	 * 解析class上面的注释
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
				lines.add(sb.toString());
				sb = new StringBuilder();
			}
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(str);
		}

		if (sb.length() > 0) {
			lines.add(sb.toString().trim());
		}
		return lines;
	}
}
