package org.nlpcn.jcoder.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * token entity
 *
 * @author Ansj
 */
public class Token implements Serializable {

	public static final Token NULL = new Token();

	private String token;

    private String userStr;

    @JSONField(serialize = false, deserialize = false)
    private User user;

	private long expiration = 30 * 60 * 1000L;

	private Date expirationTime;

	private Date createTime;

	private Map<String, Object> params;

	/**
	 * 权限数组。* 为全部
	 */
	private Set<String> authorizes = new HashSet<>();

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

    public String getUserStr() {
        return userStr;
    }

    public void setUserStr(String userStr) {
        this.userStr = userStr;
    }

    public User getUser() {
        return getUser(User.class);
    }

    public <T extends User> T getUser(Class<T> clazz) {
        if (user == null) {
            user = JSON.parseObject(userStr, clazz);
        }
        return clazz.cast(user);
    }

    public void setUser(User user) {
        userStr = JSON.toJSONString(this.user = user);
    }

	public Date getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public void addAuthorize(String authorize) {
		if (!authorize.contains("/")) {
			authorize = authorize + "/*";
		}
		authorizes.add(authorize);
	}

	public void rmAuthorize(String authorize) {
		authorizes.remove(authorize);
	}

	public boolean authorize(String className, String methodName) {
		if (authorizes.size() == 0) {
			return true;
		}
		if (authorizes.contains(className + "/*")) {
			return true;
		}
		return authorizes.contains(className + "/" + methodName);
	}

	public long getExpiration() {
		return expiration;
	}

	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

	public boolean authorize(String authorize) {
		if (authorizes.size() == 0) {
			return true;
		}
		if (!authorize.contains("/")) {
			authorize = authorize + "/*";
		}
		return authorizes.contains(authorize);
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
