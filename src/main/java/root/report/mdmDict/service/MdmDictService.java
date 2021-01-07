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

    public List<Map> getDictValueByDictId(Map m) {
        return DbFactory.Open(DbFactory.FORM).selectList("mdmDict.getDictValueByPId",m);
    }
}
