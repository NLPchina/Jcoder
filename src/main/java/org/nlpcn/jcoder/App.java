package org.nlpcn.jcoder;

import org.nlpcn.jcoder.job.SiteSetup;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.ioc.provider.ComboIocProvider;

@Modules(packages = { "org.nlpcn.jcoder.controller" }, scanPackage = true)
@IocBy(type = ComboIocProvider.class, args = { "*anno", "org.nlpcn.jcoder" })
@Encoding(input = "UTF-8", output = "UTF-8")
@SetupBy(SiteSetup.class)
@Chain("jcoder")
@ChainBy(args = {"ioc.js"})
public class App {
}
