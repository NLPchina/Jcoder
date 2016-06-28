import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;

import java.util.List;


public class GetTaskStr {
	public static void main(String[] args) {
		BasicDao dao = new BasicDao("jdbc:mysql://172.21.19.203:3306/kuyun_eye?useUnicode=true&characterEncoding=utf-8", "ky", "ky");

		List<Task> search = dao.search(Task.class, Cnd.where("name", "=", "OnlineDataWarning"));
		
		if(search.size()==1){
			System.out.println(search.get(0).getCode());
		}
	}
}
