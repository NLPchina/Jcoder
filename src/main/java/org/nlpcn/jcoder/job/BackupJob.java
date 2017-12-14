//package org.nlpcn.jcoder.job;
//
//import java.io.File;
//import java.util.Date;
//import java.util.List;
//import java.util.OptionalLong;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import com.alibaba.fastjson.util.TypeUtils;
//import org.nlpcn.jcoder.util.IOUtil;
//import org.nlpcn.jcoder.util.DateUtils;
//import org.nlpcn.jcoder.util.StaticValue;
//import org.nutz.ioc.IocException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * 每一个小时进行一次备份
// *
// * @author Ansj
// *
// */
//public class BackupJob implements Runnable {
//
//	private static final Logger LOG = LoggerFactory.getLogger(BackupJob.class);
//
//	@Override
//	public void run() {
//		File backupPath = new File(StaticValue.HOME, "backup");
//
//		if (!backupPath.isFile()) {
//			backupPath.mkdir();
//		}
//
//		while (true) {
//
//			try {
//				try {
//					Thread.sleep(3600000L);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//					return;
//				}
//
//				LOG.info("to backup tasks!");
//
//				BackupService backupService = StaticValue.getSystemIoc().get(BackupService.class, "backupService");
//
//				String backup = backupService.backup();
//
//				List<String> list = Stream.of(backupPath.list()).filter(s -> s.replace(".tasks.bak", "").matches("\\d+")).collect(Collectors.toList());
//
//				String timeStamp = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
//
//				if (list == null || list.size() == 0) {
//					IOUtil.Writer(new File(backupPath, timeStamp + ".tasks.bak").getAbsolutePath(), "utf-8", backup);
//					continue;
//				}
//
//				OptionalLong max = list.stream().mapToLong(s -> TypeUtils.castToLong(s.replace(".tasks.bak", ""))).max();
//
//				String content = IOUtil.getContent(new File(backupPath, max.getAsLong() + ".tasks.bak"), "utf-8");
//
//				if (backup.equals(content)) {
//					LOG.info("tasks backup has not change so skip");
//					continue;
//				}
//
//				IOUtil.Writer(new File(backupPath, timeStamp + ".tasks.bak").getAbsolutePath(), "utf-8", backup);
//			} catch (IocException e1) {
//				e1.printStackTrace();
//			}
//
//		}
//	}
//}
