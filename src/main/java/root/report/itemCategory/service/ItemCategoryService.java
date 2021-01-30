package root.report.itemCategory.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.report.db.DbFactory;
import root.report.mdmDict.service.MdmDictService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemCategoryService {

    private static Logger log = Logger.getLogger(ItemCategoryService.class);

    @Autowired
    private MdmDictService mdmDictService;

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
            List<Map<String, Object>> resultList = sqlSession.selectList("itemCategory.getAllPage", map, bounds);
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
    public String saveOrUpdateCategory(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> map  = new HashMap<>();
        String id="";
        map.put("category_name",jsonObject.getString("category_name"));
        map.put("category_pid",jsonObject.getString("category_pid"));
        map.put("category_code",jsonObject.getString("category_code"));
        JSONArray addLine = jsonObject.getJSONArray("lineForm");
        JSONArray addLine2 = jsonObject.getJSONArray("lineForm2");
        if(addLine.size()>0 && addLine2.size()>0) {
            addLine.addAll(addLine2);
        }else if(addLine.size()==0 && addLine2.size()>0){
            addLine=addLine2;
        }else if(addLine.size()>0 && addLine2.size()==0){

        }

        JSONArray delLine = jsonObject.getJSONArray("lineDelete");
        JSONArray delLine2 = jsonObject.getJSONArray("lineDelete2");
        if(delLine.size()>0 && delLine2.size()>0) {
            delLine.addAll(delLine2);
        }else if(delLine.size()==0 && delLine2.size()>0){
            delLine=delLine2;
        }
        if(null==jsonObject.getString("category_id")|| "".equals(jsonObject.getString("category_id"))){
            Integer newId= sqlSession.selectOne("itemCategory.getMaxId");
            newId = newId==null?1:newId;
            map.put("category_id",newId);
            sqlSession.insert("itemCategory.createMdmItemCategory",map);
            id=String.valueOf(map.get("category_id"));
            for(int i = 0; i < addLine.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = addLine.getJSONObject(i);
                Integer newVId= sqlSession.selectOne("itemCategory.getMaxValueId",newId);
                mapVal.put("category_id",newId);
                mapVal.put("row_number",newVId==null?1:newVId);
                mapVal.put("segment_name",jsonObjectVal.getString("segment_name"));
                mapVal.put("segment",jsonObjectVal.getString("segment"));
                mapVal.put("attribute",jsonObjectVal.getString("attribute"));
                mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                mapVal.put("type",jsonObjectVal.getString("type"));
                mapVal.put("valid",1);
                mapVal.put("row_or_column",jsonObjectVal.getString("row_or_column").equals("")?null:jsonObjectVal.getString("row_or_column"));
                sqlSession.insert("itemCategory.createMdmItemCategorySegment",mapVal);
            }
        }else{
            map.put("category_id",jsonObject.getString("category_id"));
            sqlSession.update("itemCategory.updateMdmItemCategory",map);
            id=jsonObject.getString("category_id");
            for(int i = 0; i < addLine.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = addLine.getJSONObject(i);
                if(jsonObjectVal.getString("row_number").contains("NEW")) {
                    Integer newVId = sqlSession.selectOne("itemCategory.getMaxValueId",id);
                    mapVal.put("category_id", id);
                    mapVal.put("row_number", newVId==null?1:newVId);
                    mapVal.put("segment_name", jsonObjectVal.getString("segment_name"));
                    mapVal.put("segment", jsonObjectVal.getString("segment"));
                    mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                    mapVal.put("type",jsonObjectVal.getString("type"));
                    mapVal.put("attribute",jsonObjectVal.getString("attribute"));
                    mapVal.put("valid",1);
                    mapVal.put("row_or_column",jsonObjectVal.getString("row_or_column").equals("")?null:jsonObjectVal.getString("row_or_column"));
                    sqlSession.insert("itemCategory.createMdmItemCategorySegment", mapVal);
                }else{
                    mapVal.put("category_id", jsonObjectVal.getString("category_id"));
                    mapVal.put("row_number",jsonObjectVal.getString("row_number"));
                    Map m= sqlSession.selectOne("itemCategory.getItemCategorySegmentById",mapVal);
                    if(null!=m) {
                        m.put("segment_name", jsonObjectVal.getString("segment_name"));
                        m.put("segment", jsonObjectVal.getString("segment"));
                        m.put("dict_id", jsonObjectVal.getString("dict_id"));
                        m.put("type",jsonObjectVal.getString("type"));
                        m.put("attribute",jsonObjectVal.getString("attribute"));
                        m.put("row_or_column", jsonObjectVal.getString("row_or_column"));
                        sqlSession.update("itemCategory.updateMdmItemCategorySegment", m);
                    }else{
                        Integer newVId = sqlSession.selectOne("itemCategory.getMaxValueId",id);
                        mapVal.put("category_id", id);
                        mapVal.put("row_number", jsonObjectVal.getString("row_number"));
                        mapVal.put("segment_name", jsonObjectVal.getString("segment_name"));
                        mapVal.put("segment", jsonObjectVal.getString("segment"));
                        mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                        mapVal.put("type",jsonObjectVal.getString("type"));
                        mapVal.put("attribute",jsonObjectVal.getString("attribute"));
                        mapVal.put("valid",1);
                        mapVal.put("row_or_column",jsonObjectVal.getString("row_or_column").equals("")?null:jsonObjectVal.getString("row_or_column"));
                        sqlSession.insert("itemCategory.createMdmItemCategorySegment", mapVal);
                    }
                }
            }
        }
        if(delLine.size()>0) {
            for (int i = 0; i < delLine.size(); i++) {
                Map<String, Object> dmap = new HashMap<>();
                JSONObject jsonObjectVal = delLine.getJSONObject(i);
                if(!jsonObjectVal.getString("row_number").contains("NEW")) {
                    dmap.put("category_id", jsonObjectVal.getString("category_id"));
                    dmap.put("row_number", jsonObjectVal.getString("row_number"));
                    sqlSession.delete("itemCategory.deleteItemCategorySegmentByID", dmap);
                }
            }
        }
        return id;
    }
    public List<Map> getItemCategorySegmentByPid(Map m) {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("itemCategory.getItemCategorySegmentDictByPId", m);
        for(int i=0;i<list.size();i++){
            Map mapp=list.get(i);
            List dictlist =new ArrayList();
            if(null!=mapp.get("dict_id") && !"".equalsIgnoreCase(mapp.get("dict_id")==null?"":mapp.get("dict_id").toString())) {
                dictlist=  mdmDictService.getDictValueListByDictId(mapp.get("dict_id").toString());
            }
            mapp.put("dictList",dictlist);
        }
        return list;
    }
    public List<Map> getItemCategorySegmentListByPid(Map m) {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("itemCategory.getItemCategorySegmentDictByPId", m);
        for(int i=0;i<list.size();i++){
            Map mapp=list.get(i);
            List dictlist= mdmDictService.getDictValueListByDictId(mapp.get("dict_id").toString());
            mapp.put("dictList",dictlist);
        }
        return list;
    }

    // 功能描述: 根据 dict_id 和 out_id 批量删除 func_dict的信息
    public void deleteItemCategoryByID(SqlSession sqlSession,String category_id){
        Map<String,Object> map=new HashMap();
        map.put("category_id",category_id);
        sqlSession.delete("itemCategory.deleteItemCategoryByID",map);
        sqlSession.delete("itemCategory.deleteItemCategorySegmentByPID",map);
    }

    public Map getItemCategoryById(Map m) {
        return DbFactory.Open(DbFactory.FORM).selectOne("itemCategory.getItemCategoryById",m);
    }

    public List<Map> getItemCategoryByPid(Map map) {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        List<Map> list= sqlSession.selectList("itemCategory.getItemCategoryByPid",map);
        for(int i=0;i<list.size();i++){
            map.put("category_pid",list.get(i).get("category_id"));
            List<Map> chilerenlist= sqlSession.selectList("itemCategory.getItemCategoryByPid",map);
            if(null!=chilerenlist && chilerenlist.size()>0){
                list.get(i).put("children",childrenListItem(sqlSession,chilerenlist));
            }
        }
        return list;
    }

    public List<Map> childrenListItem (SqlSession sqlSession,List<Map> list) {
        if (list.isEmpty()) {
            return list;
        }
        Map map=new HashMap();
        for(int i=0;i<list.size();i++){
            map.put("category_pid",list.get(i).get("category_id"));
            List<Map> chilerenlist= sqlSession.selectList("itemCategory.getItemCategoryByPid",map);
            if(null!=chilerenlist && chilerenlist.size()>0){
                list.get(i).put("children",childrenListItem(sqlSession,chilerenlist));
            }
        }
        return list;
    }

    public Integer countChildren(SqlSession sqlSession, String category_id) {
       return sqlSession.selectOne("itemCategory.countChildren",category_id);
    }


}
