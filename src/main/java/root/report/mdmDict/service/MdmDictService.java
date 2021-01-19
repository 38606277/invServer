package root.report.mdmDict.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.db.DbFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MdmDictService {

    private static Logger log = Logger.getLogger(MdmDictService.class);


    public Map<String,Object> getAllPage(Map<String,String> map) {
        Map<String,Object> map1=new HashMap<>();

        try {
            SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
            RowBounds bounds = null;
            if (map == null) {
                bounds = RowBounds.DEFAULT;
            } else {
                Integer startIndex = Integer.parseInt(map.get("startIndex").toString());
                Integer perPage = Integer.parseInt(map.get("perPage"));
                if (startIndex == 1 || startIndex == 0) {
                    startIndex = 0;
                } else {
                    startIndex = (startIndex - 1) * perPage;
                }
                bounds = new PageRowBounds(startIndex, perPage);
            }
            List<Map<String, Object>> resultList = sqlSession.selectList("mdmDict.getAllPage", map, bounds);
            Long totalSize = 0L;
            if (map != null && map.size() != 0) {
                totalSize = ((PageRowBounds) bounds).getTotal();
            } else {
                totalSize = Long.valueOf(resultList.size());
            }

            map1.put("list", resultList);
            map1.put("total", totalSize);

        }catch (Exception e){
            e.printStackTrace();
        }
        return map1;
    }
    /**
     * 功能描述: 根据JSON数据解析 对应数据，生成func_dict记录
     */
    public String saveOrUpdateDict(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map  = new HashMap<>();
        String id="";

        map.put("dict_name",jsonObject.getString("dict_name"));
        map.put("dict_type",jsonObject.getString("dict_type"));
        map.put("dict_code",jsonObject.getString("dict_code"));
        JSONArray addLine = jsonObject.getJSONArray("lineForm");
        JSONArray delLine = jsonObject.getJSONArray("lineDelete");
        if(null==jsonObject.getString("dict_id")|| "".equals(jsonObject.getString("dict_id"))){
            Integer newId= sqlSession.selectOne("mdmDict.getMaxId");
            newId = newId==null?1:newId;
            map.put("dict_id",newId);
            sqlSession.insert("mdmDict.createMdnDict",map);
            id=String.valueOf(map.get("id"));
            for(int i = 0; i < addLine.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = addLine.getJSONObject(i);
                Integer newVId= sqlSession.selectOne("mdmDict.getMaxValueId");
                mapVal.put("dict_id",newId);
                mapVal.put("value_id",newVId==null?1:newVId);
                mapVal.put("value_name",jsonObjectVal.getString("value_name"));
                mapVal.put("value_code",jsonObjectVal.getString("value_code"));
                mapVal.put("value_pid",jsonObjectVal.getString("value_pid").equals("")?null:jsonObjectVal.getString("value_pid"));
                sqlSession.insert("mdmDict.createMdnDictValue",mapVal);
            }
        }else{
            map.put("dict_id",jsonObject.getString("dict_id"));
            sqlSession.update("mdmDict.updateMdmDict",map);
            id=jsonObject.getString("dict_id");
            for(int i = 0; i < addLine.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = addLine.getJSONObject(i);
                if(jsonObjectVal.getString("value_id").contains("NEW")) {
                    Integer newVId = sqlSession.selectOne("mdmDict.getMaxValueId");
                    mapVal.put("dict_id", id);
                    mapVal.put("value_id", newVId==null?1:newVId);
                    mapVal.put("value_name", jsonObjectVal.getString("value_name"));
                    mapVal.put("value_code", jsonObjectVal.getString("value_code"));
                    mapVal.put("value_pid", jsonObjectVal.getString("value_pid").equals("") ? null : jsonObjectVal.getString("value_pid"));
                    sqlSession.insert("mdmDict.createMdnDictValue", mapVal);
                }else{
                    Map m= sqlSession.selectOne("mdmDict.getDictValueById",jsonObjectVal.getString("value_id"));
                    m.put("value_name", jsonObjectVal.getString("value_name"));
                    m.put("value_code", jsonObjectVal.getString("value_code"));
                    sqlSession.update("mdmDict.updateMdmDictValue",m);
                }
            }
        }
        if(delLine.size()>0) {
            for (int i = 0; i < delLine.size(); i++) {
                Map<String, Object> dmap = new HashMap<>();
                JSONObject jsonObjectVal = addLine.getJSONObject(i);
                if(!jsonObjectVal.getString("value_id").contains("NEW")) {
                    dmap.put("value_id", jsonObjectVal.getString("value_id"));
                    sqlSession.delete("mdmDict.deleteDictValueByID", dmap);
                }
            }
        }
        return id;
    }

    public String saveOrUpdateDictTree(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map  = new HashMap<>();
        String dict_id="";
        map.put("dict_name",jsonObject.getString("dict_name"));
        map.put("dict_type",jsonObject.getString("dict_type"));
        map.put("dict_code",jsonObject.getString("dict_code"));
        JSONArray addLine = jsonObject.getJSONArray("lineForm");
        JSONArray delLine = jsonObject.getJSONArray("lineDelete");
        Integer dictId=null;
        Map mapKeyId=new HashMap();
        if(null==jsonObject.getString("dict_id")|| "".equals(jsonObject.getString("dict_id"))){
            dictId= sqlSession.selectOne("mdmDict.getMaxId");
            dictId = dictId==null?1:dictId;
            map.put("dict_id",dictId);
            sqlSession.insert("mdmDict.createMdnDict",map);
          //  dict_id=String.valueOf(map.get("dict_id"));
            Integer newVId=null;
            newVId= sqlSession.selectOne("mdmDict.getMaxValueId");
            newVId = newVId==null?0:newVId;
            for(int i = 0; i < addLine.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = addLine.getJSONObject(i);
                newVId++;
                mapVal.put("dict_id",dictId);
                mapVal.put("value_id",newVId==null?1:newVId);
                mapKeyId.put(jsonObjectVal.getString("value_id"),newVId);
                mapVal.put("value_name",jsonObjectVal.getString("value_name"));
                mapVal.put("value_code",jsonObjectVal.getString("value_code"));
                mapVal.put("value_pid",jsonObjectVal.getString("value_pid").equals("")?null:jsonObjectVal.getString("value_pid"));
                sqlSession.insert("mdmDict.createMdnDictValue",mapVal);
                if(null!=jsonObjectVal.getJSONArray("children")) {
                    JSONArray objChildren = jsonObjectVal.getJSONArray("children");
                    newVId = insertDictValueListItem(sqlSession,objChildren,mapKeyId,newVId,dictId);
                }
            }
        }else{
            map.put("dict_id",jsonObject.getString("dict_id"));
            sqlSession.update("mdmDict.updateMdmDict",map);
            dictId=Integer.parseInt(jsonObject.getString("dict_id"));
            for(int i = 0; i < addLine.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = addLine.getJSONObject(i);
                if(jsonObjectVal.getString("value_id").contains("NEW")) {
                    Integer newVId = sqlSession.selectOne("mdmDict.getMaxValueId");
                    newVId = newVId==null?0:newVId;
                    newVId++;
                    mapVal.put("dict_id", dictId);
                    mapVal.put("value_id", newVId);
                    mapKeyId.put(jsonObjectVal.getString("value_id"),newVId);
                    mapVal.put("value_name", jsonObjectVal.getString("value_name"));
                    mapVal.put("value_code", jsonObjectVal.getString("value_code"));
                    mapVal.put("value_pid", jsonObjectVal.getString("value_pid").equals("") ? null : jsonObjectVal.getString("value_pid"));
                    mapKeyId.put(jsonObjectVal.getString("value_id"),newVId);
                    sqlSession.insert("mdmDict.createMdnDictValue", mapVal);
                    if(null!=jsonObjectVal.getJSONArray("children")) {
                        JSONArray objChildren = jsonObjectVal.getJSONArray("children");
                         insertDictValueListItem(sqlSession,objChildren,mapKeyId,newVId,dictId);
                    }

                }else{
                    Map m= sqlSession.selectOne("mdmDict.getDictValueById",jsonObjectVal.getString("value_id"));
                    m.put("value_name", jsonObjectVal.getString("value_name"));
                    m.put("value_code", jsonObjectVal.getString("value_code"));
                    sqlSession.update("mdmDict.updateMdmDictValue",m);
                    mapKeyId.put(jsonObjectVal.getString("value_id"),jsonObjectVal.getString("value_id"));
                    if(null!=jsonObjectVal.getJSONArray("children")) {
                        JSONArray objChildren = jsonObjectVal.getJSONArray("children");
                        updateDictValueListItem(sqlSession,objChildren,mapKeyId,Integer.parseInt(m.get("value_id").toString()),dictId);
                    }
                }
            }
        }
        if(delLine.size()>0) {
            for (int i = 0; i < delLine.size(); i++) {
                Map<String, Object> dmap = new HashMap<>();
                String jsonObjectVal = delLine.get(i).toString();
                if(!jsonObjectVal.contains("NEW")) {
                    dmap.put("value_id", jsonObjectVal);
                    sqlSession.delete("mdmDict.deleteDictValueByID", dmap);
                }
            }
        }
        return dict_id;
    }

    public Integer insertDictValueListItem (SqlSession sqlSession,JSONArray childrenList,Map mapKeyId,Integer newVId,Integer dictId) {
        if (childrenList.isEmpty()) {
            return newVId;
        }
        for (int i = 0; i < childrenList.size(); i++) {
            newVId++;
            JSONObject obj = childrenList.getJSONObject(i);
            Map mapVal=new HashMap();
            mapVal.put("dict_id",dictId);
            mapVal.put("value_id",newVId);
            mapVal.put("value_name",obj.getString("value_name"));
            mapVal.put("value_code",obj.getString("value_code"));
            mapVal.put("value_pid",mapKeyId.get(obj.getString("value_pid")));
            mapKeyId.put(obj.getString("value_id"),newVId);
            sqlSession.insert("mdmDict.createMdnDictValue",mapVal);
            if(null!=obj.getJSONArray("children")) {
                JSONArray objChildren = obj.getJSONArray("children");
                newVId= insertDictValueListItem(sqlSession,objChildren,mapKeyId,newVId,dictId);
            }
        }
        return newVId;
    }

    public Integer updateDictValueListItem (SqlSession sqlSession,JSONArray childrenList,Map mapKeyId,Integer newVId,Integer dictId) {
        if (!childrenList.isEmpty()) {
            for (int i = 0; i < childrenList.size(); i++) {
                JSONObject obj = childrenList.getJSONObject(i);
                if (obj.getString("value_id").contains("NEW")) {
                    Map<String, Object> mapVal = new HashMap<>();
                    Integer newVVId = sqlSession.selectOne("mdmDict.getMaxValueId");
                    newVVId = newVVId == null ? 0 : newVVId;
                    newVVId++;
                    mapVal.put("dict_id", dictId);
                    mapVal.put("value_id", newVVId);
                    mapKeyId.put(obj.getString("value_id"), newVVId);
                    mapVal.put("value_name", obj.getString("value_name"));
                    mapVal.put("value_code", obj.getString("value_code"));
                    mapVal.put("value_pid", mapKeyId.get(obj.get("value_pid").toString()));
                    sqlSession.insert("mdmDict.createMdnDictValue", mapVal);
                    if (null != obj.getJSONArray("children")) {
                        JSONArray objChildren = obj.getJSONArray("children");
                        insertDictValueListItem(sqlSession, objChildren, mapKeyId, newVVId, dictId);
                    }
                } else {
                    Map m = sqlSession.selectOne("mdmDict.getDictValueById", obj.getString("value_id"));
                    m.put("value_name", obj.getString("value_name"));
                    m.put("value_code", obj.getString("value_code"));
                    mapKeyId.put(obj.getString("value_id"), obj.getString("value_id"));
                    sqlSession.update("mdmDict.updateMdmDictValue", m);
                    if (null != obj.getJSONArray("children")) {
                        JSONArray objChildren = obj.getJSONArray("children");
                        updateDictValueListItem(sqlSession, objChildren, mapKeyId, Integer.parseInt(m.get("value_id").toString()), dictId);
                    }
                }
            }
        }
        return newVId;
    }


    // 功能描述: 根据 dict_id 和 out_id 批量删除 func_dict的信息
    public void deleteDictById(SqlSession sqlSession,String dict_id){
        Map<String,Object> map=new HashMap();
        map.put("dict_id",dict_id);
        sqlSession.delete("mdmDict.deleteDictByID",map);
        sqlSession.delete("mdmDict.deleteDictValueByPID",map);
    }

    public Map getDictByID(Map m) {
        return DbFactory.Open(DbFactory.FORM).selectOne("mdmDict.getDictById",m);
    }

    public List<Map> getDictValueListByDictId(String dictId) {
        return DbFactory.Open(DbFactory.FORM).selectList("mdmDict.getDictValueListByDictId",dictId);
    }

    public List<Map> getDictValueByDictId(Map m) {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        List<Map> list= sqlSession.selectList("mdmDict.getDictValueByPId",m);
        for(int i=0;i<list.size();i++){
            Map map=new HashMap();
            map.put("value_id",list.get(i).get("value_id"));
            List<Map> chilerenlist= sqlSession.selectList("mdmDict.getDictValueByValuePId",map);
            if(null!=chilerenlist && chilerenlist.size()>0){
                list.get(i).put("children",childrenListItem(sqlSession,chilerenlist));
            }
        }
        return list;
    }

    public List<Map> getDictValueListByDictId(Map m) {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        List<Map> list= sqlSession.selectList("mdmDict.getDictValueListByDictId",m);

        return list;
    }

    public List<Map> childrenListItem (SqlSession sqlSession,List<Map> list) {
        if (list.isEmpty()) {
            return list;
        }
        for(int i=0;i<list.size();i++){
            Map map=new HashMap();
            map.put("value_id",list.get(i).get("value_id"));
            List<Map> chilerenlist= sqlSession.selectList("mdmDict.getDictValueByValuePId",map);
            if(null!=chilerenlist && chilerenlist.size()>0){
                list.get(i).put("children",childrenListItem(sqlSession,chilerenlist));
            }
        }
        return list;
    }


    public List<Map> getAll(JSONObject map) {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        List<Map> chilerenlist= sqlSession.selectList("mdmDict.getAllPage",map);
        return chilerenlist;
    }

    public Map getDictByCode(Map map) {
        return DbFactory.Open(DbFactory.FORM).selectOne("mdmDict.getDictByCode",map);
    }
}
