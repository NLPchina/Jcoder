package org.nlpcn.jcoder.util.appender;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.nlpcn.jcoder.util.StaticValue;

import java.io.Serializable;

/**
 * log4j jcoder 日志收集程序
 */
@Plugin(name = "Jcoder", category = "Core", elementType = "appender", printObject = true)
public class JcoderAppender extends AbstractAppender {

	protected JcoderAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
		super(name, filter, layout);
	}

	@Override
	public void append(LogEvent event) {
		if (StaticValue.space() == null) {
			return;
		}
		StaticValue.space().getRoomService().sendMessage("jcoder_log", event.getMessage().getFormattedMessage());
	}

	// 下面这个方法可以接收配置文件中的参数信息
	@PluginFactory
	public static JcoderAppender createAppender(@PluginAttribute("name") String name,
	                                            @PluginElement("Filter") final Filter filter,
	                                            @PluginElement("Layout") Layout<? extends Serializable> layout,
	                                            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
		if (name == null) {
			LOGGER.error("No name provided for MyCustomAppenderImpl");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new JcoderAppender(name, filter, layout);
	}
}
