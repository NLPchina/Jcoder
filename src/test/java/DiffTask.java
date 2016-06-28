import org.nlpcn.commons.lang.util.FileFinder;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.lang.stream.StringReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将所有action导入到当前目录
 *
 * @author ansj
 */
public class DiffTask {

    private static List<String> diff = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        BasicDao dao = new BasicDao("jdbc:mysql://172.21.19.203:3306/kuyun_eye?useUnicode=true&characterEncoding=utf-8", "ky", "ky");
        File base = new File(FileFinder.find("kuyun-data/test"), "com/kuyun/data/code"); // 程序目录

        List<Task> search = dao.search(Task.class, "id");

        for (Task task : search) {
            diff2File(base, task);
        }

        System.out.println(diff);
    }

    private static void diff2File(File base, Task task) throws IOException {

        if (!task.getCodeType().equals("java")) {
            System.err.println("ignore " + task.getName());
            return;
        }

        String code = task.getCode();

        String path = JavaSourceUtil.findPackage(code);

        String className = JavaSourceUtil.findClassName(code);

        File codeDir = null;
        if (!"com.kuyun.data.code".equals(path)) {
            codeDir = new File(base, path.replace("com.kuyun.data.code.", "").replace(".", "/"));
        } else {
            codeDir = base;
        }

        // 创建目录
        codeDir.mkdirs();

        File codeFile = new File(codeDir, className + ".java");

        if (codeFile.isFile()) {
            List<String> codeList = IOUtil.readFile2List(codeFile, "UTF-8");

            List<String> contentList = IOUtil.readFile2List(new BufferedReader(new StringReader(code)));

            if (codeList.size() == contentList.size()) {
                boolean flag = true;
                for (int i = 0; i < codeList.size(); i++) {
                    if (!codeList.get(i).equals(contentList.get(i))) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    return;
                }
            }

            System.out.println("diff " + task.getName());

            diff.add(task.getName());
        }

    }
}
