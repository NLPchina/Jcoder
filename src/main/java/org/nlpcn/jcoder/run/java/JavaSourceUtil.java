package org.nlpcn.jcoder.run.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.util.JavaDocUtil;
import org.nlpcn.jcoder.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSourceUtil {

	private String className = null;

	private String pack = null;

	private List<String> executeMethod = new ArrayList<>();

	public JavaSourceUtil(String code) throws CodeException {
		try {
			CompilationUnit compile = JavaDocUtil.compile(code);

			PackageDeclaration aPackage = compile.getPackage();

			if(aPackage==null){
				throw new CodeException("package can not null ");
			}
			pack = aPackage.getPackageName();
			if (StringUtil.isBlank(pack)) {
				throw new CodeException("package can not empty ");
			}

			List<TypeDeclaration> types = compile.getTypes();

			for (TypeDeclaration type : types) {
				if (type.getModifiers() == Modifier.PUBLIC) {
					if (className != null) {
						throw new CodeException("class not have more than one public class ");//这个情况不会发生
					}
					className = type.getName();
				}
			}

			if (className == null) {
				throw new CodeException("not find className ");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new CodeException(e.getMessage());
		}


	}


	public String getPackage() {
		return pack;
	}

	public String getFullName() throws IOException {
		return pack + "." + className;
	}

	/**
	 * 根据一个类返回类名称
	 *
	 * @return 类名称
	 * @throws IOException
	 */
	public String getClassName() {
		return className;
	}
}
