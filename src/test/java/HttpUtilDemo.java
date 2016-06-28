import org.nlpcn.jcoder.util.HttpUtils;

import java.util.HashMap;

public class HttpUtilDemo {
	public static void main(String[] args) {

		HashMap<String, String> maps = new HashMap<>();

		maps.put("Accept", "application/json, text/javascript, */*; q=0.01");

		String jsonStr = HttpUtils.getJSONStr("http://baike.sogou.com/baike/recommandRelation/65544676.v", maps);

		System.out.println(jsonStr);
	}
}
