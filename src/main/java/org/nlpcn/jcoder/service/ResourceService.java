package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.Param;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Administrator on 2017/12/22.
 */
@IocBean
public class ResourceService {
    private SharedSpaceService sharedSpaceService;

    public ResourceService() {
        this.sharedSpaceService = StaticValue.space();
    }

    public void getResourceFiles(JSONArray nodes, String groupName, String path, String pId) throws Exception {
        JSONArray jsonArray = new JSONArray();
        if (StringUtil.isBlank(path)) {
            path = "/file";
        } else {
            path = "/file" + path;
        }

        List<String> files = StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.GROUP_PATH + "/" + groupName + path);
        if (files != null || files.size() > 0) {
            for (String s : files) {
                byte[] data2ZK = StaticValue.space().getData2ZK(SharedSpaceService.GROUP_PATH + "/" + groupName + path + "/" + s);
                if (data2ZK == null || data2ZK.length == 0) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name",s);
                    UUID uuid = UUID.randomUUID();
                    jsonObject.put("id", uuid.toString());
                    jsonObject.put("open",true);
                    jsonObject.put("pId",pId);
                    File file = new File(StaticValue.GROUP_FILE, groupName + path.replace("/file","")+"/"+s);
                    FileInfo fileInfo = new FileInfo(file);
                    JSONObject fi = JSONObject.parseObject(JSONObject.toJSONString(fileInfo));
                    fi.put("date",fileInfo.lastModified());
                    jsonObject.put("file",fi);
                    nodes.add(jsonObject);
                    getResourceFiles(nodes, groupName, path.replace("/file","")+"/"+s, uuid.toString());
                }else{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name",s);
                    UUID uuid = UUID.randomUUID();
                    jsonObject.put("id", uuid.toString());
                    jsonObject.put("open",false);
                    jsonObject.put("pId",pId);
                    File file = new File(StaticValue.GROUP_FILE, groupName + path.replace("/file","")+"/"+s);
                    FileInfo fileInfo = new FileInfo(file);
                    JSONObject fi = JSONObject.parseObject(JSONObject.toJSONString(fileInfo));
                    fi.put("date",fileInfo.lastModified());
                    jsonObject.put("file",fi);
                    nodes.add(jsonObject);
                }
                /*FileInfo fileInfo = new FileInfo();
                    fileInfo.setFile(new File(StaticValue.GROUP_FILE, groupName + path.replace("/file","")+"/"+s));
                    fileInfo.setDirectory(true);
                    fileInfo.setLength(0);
                    fileInfo.setName(s);
                    fileInfo.setRelativePath(path.replace("/file","")+"/"+s);*/
                /*fileInfo.setMd5(fi.getMd5());
                    fileInfo.setLength(fi.getMd5().getBytes("utf-8").length);
                    fileInfo.setName(s);
                    fileInfo.setRelativePath(path.replace("/file","")+"/"+s);
                    JSONObject fileInfos = JSONObject.parseObject(JSONObject.toJSONString(fileInfo));
                    fileInfos.put("date",fileInfo.lastModified());*/
                /*File file = new File(StaticValue.GROUP_FILE, groupName + path.replace("/file","")+"/"+s);
                    FileInfo fileInfo = new FileInfo(file);
                    byte[] data2ZK = space.getData2ZK(space.GROUP_PATH + "/" + groupName + "/file/lib/pom.xml");
                    if (data2ZK == null) return "";
                    FileInfo fileInfo = JSONObject.parseObject(data2ZK, FileInfo.class);
                    return fileInfo.getMd5();
                    */
                    /*FileInfo fileInfo = new FileInfo();
                    fileInfo.setFile(new File(StaticValue.GROUP_FILE, groupName + path.replace("/file","")+"/"+s));
                    fileInfo.setDirectory(true);*/
                     /*List<String> childrens = StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.GROUP_PATH + "/" + groupName + path+ "/" + s);
                    Set<String> set = new HashSet<>() ;
                    StaticValue.space().walkDataNode(set , SharedSpaceService.GROUP_PATH+"/"+groupName+"/file");*/
            }
        }
    }

    /**
     * 获取pom文件内容
     *
     * @param groupName
     * @return
     * @throws Exception
     */
    public FileInfo getFileInfo(String groupName,String path) throws Exception {
        SharedSpaceService space = StaticValue.space();
        byte[] data2ZK = space.getData2ZK(space.GROUP_PATH + "/" + groupName + "/file"+path);
        if (data2ZK == null) return null;
        return JSONObject.parseObject(data2ZK, FileInfo.class);
    }

    /**
     * 创建文件夹
     *
     * @param groupName
     * @param path
     * @param folderName
     */
    public void createFolder(String groupName, String path,String folderName){
        if (StringUtil.isBlank(path)) {
            path = "/";
        }
        File dir = new File(StaticValue.GROUP_FILE, groupName + path + folderName);
        dir.mkdir();
    }

    /**
     * 创建文件夹到ZK
     * @param groupName
     * @param path
     * @param folderName
     */
    public void createFolder2ZK(String groupName,String path,String folderName){
        /*FileInfo fileInfo = new FileInfo();
        fileInfo.setFile(new File(StaticValue.GROUP_FILE, groupName + "/lib/pom.xml"));
        fileInfo.setMd5(code);
        fileInfo.setDirectory(false);
        fileInfo.setLength(code.getBytes("utf-8").length);
        fileInfo.setName("pom.xml");
        fileInfo.setRelativePath("lib/pom.xml");*/

        //StaticValue.space().getZk().setData().forPath(SharedSpaceService.GROUP_PATH + "/" + groupName + "/file/lib/pom.xml", JSONObject.toJSONBytes(fileInfo));
    }
}
