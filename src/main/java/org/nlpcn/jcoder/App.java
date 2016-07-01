package org.nlpcn.jcoder;

import org.nlpcn.jcoder.job.SiteSetup;
import org.nutz.mvc.annotation.Encoding;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.ioc.provider.AnnotationIocProvider;

@Modules(packages = { "org.nlpcn.jcoder.controller" }, scanPackage = true)
@IocBy(type = AnnotationIocProvider.class, args = { "org.nlpcn.jcoder" })
@Encoding(input = "UTF-8", output = "UTF-8")
@SetupBy(SiteSetup.class)
public class App {

}
