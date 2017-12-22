package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.List;

/**
 * Created by Administrator on 2017/12/22.
 */
@IocBean
public class ResourceService {
    private SharedSpaceService sharedSpaceService;

    public ResourceService() {
        this.sharedSpaceService = StaticValue.space();
    }

}
