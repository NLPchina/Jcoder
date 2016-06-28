package org.nlpcn.jcoder.controller;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.UserGroup;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import java.util.Date;
import java.util.List;

@IocBean
@Filters(@By(type = AuthoritiesManager.class, args = {"userType", "1", "/login.jsp"}))
public class GroupAction {

    private Logger log = Logger.getLogger(this.getClass());

    public BasicDao basicDao = StaticValue.systemDao;

    @At("/auth/delUserGroup")
    @Ok("raw")
    @Fail("jsp:/fail.jsp")
    public boolean delUserGroup(@Param("id") long id) throws Exception {
        boolean flag = basicDao.delById(id, UserGroup.class);
        if (flag) {
            log.info("add userGroup which id:" + id);
        }
        return flag;
    }

    @At("/auth/updateUserGroup")
    @Ok("raw")
    public boolean updateUserGroup(@Param("groupId") Long groupId, @Param("auth") Integer auth, @Param("userId") Long userId) throws Exception {
        Condition con = Cnd.where("groupId", "=", groupId).and("userId", "=", userId);
        UserGroup userGroup = basicDao.findByCondition(UserGroup.class, con);
        if (userGroup == null) {
            userGroup = new UserGroup();
            userGroup.setUserId(userId);
            userGroup.setGroupId(groupId);
            userGroup.setCreateTime(new Date());
        }

        if (auth == 0) {
            basicDao.delById(userGroup.getId(), UserGroup.class);
        } else {
            userGroup.setAuth(auth);
            if (userGroup.getId() == null) {
                basicDao.save(userGroup);
            } else {
                basicDao.update(userGroup);
            }
        }

        return true;
    }

    /**
     * 列出用户的所有权限
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @At("/auth/authUser")
    @Ok("json")
    public List<UserGroup> authUser(@Param("userId") long userId) throws Exception {
        return basicDao.search(UserGroup.class, Cnd.where("userId", "=", userId));
    }
}
