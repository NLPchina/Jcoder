package org.nlpcn.jcoder;

import org.nlpcn.jcoder.job.SiteSetup;
import org.nutz.mvc.annotation.Encoding;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.ioc.provider.ComboIocProvider;

@Modules(packages = {"org.nlpcn.jcoder.controller"}, scanPackage = true)
@IocBy(type = ComboIocProvider.class, args = {"*org.nutz.ioc.loader.json.JsonLoader", "*org.nutz.ioc.loader.annotation.AnnotationIocLoader", "org.nlpcn.jcoder"})
@Encoding(input = "UTF-8", output = "UTF-8")
@SetupBy(SiteSetup.class)
public class App {

}

