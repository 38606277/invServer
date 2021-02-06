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
        JSONArray segmentList = jsonObject.getJSONArray("lineForm");
        JSONArray attributeList = jsonObject.getJSONArray("lineForm2");

        JSONArray delSegment = jsonObject.getJSONArray("lineDelete");
        JSONArray delAttribute = jsonObject.getJSONArray("lineDelete2");

        if(null==jsonObject.getString("category_id")|| "".equals(jsonObject.getString("category_id"))){
            Integer newId= sqlSession.selectOne("itemCategory.getMaxId");
            newId = newId==null?1:newId;
            map.put("category_id",newId);
            sqlSession.insert("itemCategory.createMdmItemCategory",map);
            id=String.valueOf(map.get("category_id"));
            //segmentList 进行保存
            for(int i = 0; i < segmentList.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = segmentList.getJSONObject(i);
                Integer newVId= sqlSession.selectOne("itemCategory.getSegmentMaxValueId",newId);
                mapVal.put("category_id",newId);
                mapVal.put("row_number",newVId==null?1:newVId);
                mapVal.put("segment_name",jsonObjectVal.getString("segment_name"));
                mapVal.put("segment",jsonObjectVal.getString("segment"));
                mapVal.put("input_mode",jsonObjectVal.getString("input_mode"));
                mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                mapVal.put("qualifier",jsonObjectVal.getString("qualifier"));
                mapVal.put("valid",1);
                mapVal.put("spread_mode",jsonObjectVal.getString("spread_mode").equals("")?null:jsonObjectVal.getString("spread_mode"));
                sqlSession.insert("itemCategory.createMdmItemCategorySegment",mapVal);
            }
            //attributeList 进行保存
            for(int i = 0; i < attributeList.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = attributeList.getJSONObject(i);
                Integer newVId= sqlSession.selectOne("itemCategory.getAttributeMaxValueId",newId);
                mapVal.put("category_id",newId);
                mapVal.put("row_number",newVId==null?1:newVId);
                mapVal.put("attribute_name",jsonObjectVal.getString("attribute_name"));
                mapVal.put("attribute",jsonObjectVal.getString("attribute"));
                mapVal.put("input_mode",jsonObjectVal.getString("input_mode"));
                mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                mapVal.put("qualifier",jsonObjectVal.getString("qualifier"));
                mapVal.put("valid",1);
                mapVal.put("spread_mode",jsonObjectVal.getString("spread_mode").equals("")?null:jsonObjectVal.getString("spread_mode"));
                mapVal.put("required",jsonObjectVal.getString("required").equals("")?0:jsonObjectVal.getString("required"));
                sqlSession.insert("itemCategory.createMdmItemCategoryAttribute",mapVal);
            }
        }else{
            map.put("category_id",jsonObject.getString("category_id"));
            sqlSession.update("itemCategory.updateMdmItemCategory",map);
            id=jsonObject.getString("category_id");
            for(int i = 0; i < segmentList.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = segmentList.getJSONObject(i);
                if(jsonObjectVal.getString("row_number").contains("NEW")) {
                    Integer newVId = sqlSession.selectOne("itemCategory.getSegmentMaxValueId",id);
                    mapVal.put("category_id", id);
                    mapVal.put("row_number", newVId==null?1:newVId);
                    mapVal.put("segment_name",jsonObjectVal.getString("segment_name"));
                    mapVal.put("segment",jsonObjectVal.getString("segment"));
                    mapVal.put("input_mode",jsonObjectVal.getString("input_mode"));
                    mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                    mapVal.put("qualifier",jsonObjectVal.getString("qualifier"));
                    mapVal.put("valid",1);
                    mapVal.put("spread_mode",jsonObjectVal.getString("spread_mode").equals("")?null:jsonObjectVal.getString("spread_mode"));
                    sqlSession.insert("itemCategory.createMdmItemCategorySegment", mapVal);
                }else{
                    mapVal.put("category_id", jsonObjectVal.getString("category_id"));
                    mapVal.put("row_number",jsonObjectVal.getString("row_number"));
                    Map m= sqlSession.selectOne("itemCategory.getItemCategorySegmentById",mapVal);
                    if(null!=m) {
                        m.put("segment_name",jsonObjectVal.getString("segment_name"));
                        m.put("segment",jsonObjectVal.getString("segment"));
                        m.put("input_mode",jsonObjectVal.getString("input_mode"));
                        m.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                        m.put("qualifier",jsonObjectVal.getString("qualifier"));
                        m.put("spread_mode",jsonObjectVal.getString("spread_mode").equals("")?null:jsonObjectVal.getString("spread_mode"));
                        sqlSession.update("itemCategory.updateMdmItemCategorySegment", m);
                    }else{
                        Integer newVId = sqlSession.selectOne("itemCategory.getSegmentMaxValueId",id);
                        mapVal.put("category_id", id);
                        mapVal.put("row_number", jsonObjectVal.getString("row_number"));
                        mapVal.put("segment_name",jsonObjectVal.getString("segment_name"));
                        mapVal.put("segment",jsonObjectVal.getString("segment"));
                        mapVal.put("input_mode",jsonObjectVal.getString("input_mode"));
                        mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                        mapVal.put("qualifier",jsonObjectVal.getString("qualifier"));
                        mapVal.put("valid",1);
                        mapVal.put("spread_mode",jsonObjectVal.getString("spread_mode").equals("")?null:jsonObjectVal.getString("spread_mode"));
                        sqlSession.insert("itemCategory.createMdmItemCategorySegment", mapVal);
                    }
                }
            }

            //attributeList 编辑保存
            for(int i = 0; i < attributeList.size(); i++){
                Map<String,Object> mapVal  = new HashMap<>();
                JSONObject jsonObjectVal = attributeList.getJSONObject(i);
                if(jsonObjectVal.getString("row_number").contains("NEW")) {
                    Integer newVId = sqlSession.selectOne("itemCategory.getAttributeMaxValueId",id);
                    mapVal.put("category_id", id);
                    mapVal.put("row_number", newVId==null?1:newVId);
                    mapVal.put("attribute_name",jsonObjectVal.getString("attribute_name"));
                    mapVal.put("attribute",jsonObjectVal.getString("attribute"));
                    mapVal.put("input_mode",jsonObjectVal.getString("input_mode"));
                    mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                    mapVal.put("qualifier",jsonObjectVal.getString("qualifier"));
                    mapVal.put("valid",1);
                    mapVal.put("spread_mode",jsonObjectVal.getString("spread_mode").equals("")?null:jsonObjectVal.getString("spread_mode"));
                    mapVal.put("required",jsonObjectVal.getString("required").equals("")?null:jsonObjectVal.getString("required"));
                    sqlSession.insert("itemCategory.createMdmItemCategoryAttribute", mapVal);
                }else{
                    mapVal.put("category_id", jsonObjectVal.getString("category_id"));
                    mapVal.put("row_number",jsonObjectVal.getString("row_number"));
                    Map m= sqlSession.selectOne("itemCategory.getItemCategoryAttributeById",mapVal);
                    if(null!=m) {
                        m.put("attribute_name",jsonObjectVal.getString("attribute_name"));
                        m.put("attribute",jsonObjectVal.getString("attribute"));
                        m.put("input_mode",jsonObjectVal.getString("input_mode"));
                        m.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                        m.put("qualifier",jsonObjectVal.getString("qualifier"));
                        m.put("spread_mode",jsonObjectVal.getString("spread_mode").equals("")?null:jsonObjectVal.getString("spread_mode"));
                        m.put("required",jsonObjectVal.getString("required").equals("")?null:jsonObjectVal.getString("required"));
                        sqlSession.update("itemCategory.updateMdmItemCategoryAttribute", m);
                    }else{
                        Integer newVId = sqlSession.selectOne("itemCategory.getAttributeMaxValueId",id);
                        mapVal.put("category_id", id);
                        mapVal.put("row_number", newVId);
                        mapVal.put("attribute_name",jsonObjectVal.getString("attribute_name"));
                        mapVal.put("attribute",jsonObjectVal.getString("attribute"));
                        mapVal.put("input_mode",jsonObjectVal.getString("input_mode"));
                        mapVal.put("dict_id",jsonObjectVal.getString("dict_id").equals("")?null:jsonObjectVal.getString("dict_id"));
                        mapVal.put("qualifier",jsonObjectVal.getString("qualifier"));
                        mapVal.put("valid",1);
                        mapVal.put("spread_mode",jsonObjectVal.getString("spread_mode").equals("")?null:jsonObjectVal.getString("spread_mode"));
                        mapVal.put("required",jsonObjectVal.getString("required").equals("")?null:jsonObjectVal.getString("required"));
                        sqlSession.insert("itemCategory.createMdmItemCategoryAttribute", mapVal);
                    }
                }
            }
        }
        if(delSegment.size()>0) {
            for (int i = 0; i < delSegment.size(); i++) {
                Map<String, Object> dmap = new HashMap<>();
                JSONObject jsonObjectVal = delSegment.getJSONObject(i);
                if(!jsonObjectVal.getString("row_number").contains("NEW")) {
                    dmap.put("category_id", jsonObjectVal.getString("category_id"));
                    dmap.put("row_number", jsonObjectVal.getString("row_number"));
                    sqlSession.delete("itemCategory.deleteItemCategorySegmentByID", dmap);
                }
            }
        }
        if(delAttribute.size()>0) {
            for (int i = 0; i < delAttribute.size(); i++) {
                Map<String, Object> dmap = new HashMap<>();
                JSONObject jsonObjectVal = delAttribute.getJSONObject(i);
                if(!jsonObjectVal.getString("row_number").contains("NEW")) {
                    dmap.put("category_id", jsonObjectVal.getString("category_id"));
                    dmap.put("row_number", jsonObjectVal.getString("row_number"));
                    sqlSession.delete("itemCategory.deleteItemCategoryAttributeByID", dmap);
                }
            }
        }
        return id;
    }
    /**
     * 单个增加
     * */
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

    /**
     * 批量 segment
     * 限定词
     * */
    public List<Map> getItemCategorySegmentBatchByPId(Map m) {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("itemCategory.getItemCategorySegmentDictBatchByPId", m);
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



    /**
     * 获取属性信息 attribute
     * */
    public List<Map> getItemCategoryAttributeByPid(Map m) {
        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("itemCategory.getItemCategoryAttributeDictByPId", m);
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

//    public List<Map> getItemCategorySegmentListByPid(Map m) {
//        List<Map> list = DbFactory.Open(DbFactory.FORM).selectList("itemCategory.getItemCategorySegmentDictByPId", m);
//        for(int i=0;i<list.size();i++){
//            Map mapp=list.get(i);
//            List dictlist= mdmDictService.getDictValueListByDictId(mapp.get("dict_id").toString());
//            mapp.put("dictList",dictlist);
//        }
//        return list;
//    }

    // 功能描述: 根据 dict_id 和 out_id 批量删除 func_dict的信息
    public void deleteItemCategoryByID(SqlSession sqlSession,String category_id){
        Map<String,Object> map=new HashMap();
        map.put("category_id",category_id);
        sqlSession.delete("itemCategory.deleteItemCategoryByID",map);
        sqlSession.delete("itemCategory.deleteItemCategorySegmentByPID",map);
        sqlSession.delete("itemCategory.deleteItemCategoryAttributeByPID",map);
    }

    public Map getItemCategoryById(Map m) {
        return DbFactory.Open(DbFactory.FORM).selectOne("itemCategory.getItemCategoryById",m);
    }

    public List<Map> getItemCategorySegmentByCatId(String catId) {
        Map map=new HashMap();
        map.put("category_id",catId);
        return  DbFactory.Open(DbFactory.FORM).selectList("itemCategory.getItemCategorySegmentByPId",map);
    }

    public List<Map> getItemCategoryAttributeByCatId(String catId) {
        Map map=new HashMap();
        map.put("category_id",catId);
        return  DbFactory.Open(DbFactory.FORM).selectList("itemCategory.getItemCategoryAttributeByPId",map);
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
            map.put("category_id",list.get(i).get("category_id"));
            List<Map> segmentlist= sqlSession.selectList("itemCategory.getItemCategorySegmentByPId",map);
            if(null!=segmentlist && segmentlist.size()>0){
                list.get(i).put("segmentlist",segmentlist);
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
            map.put("category_id",list.get(i).get("category_id"));
            List<Map> segmentlist= sqlSession.selectList("itemCategory.getItemCategorySegmentByPId",map);
            if(null!=segmentlist && segmentlist.size()>0){
                list.get(i).put("segmentlist",segmentlist);
            }
        }
        return list;
    }

    public Integer countChildren(SqlSession sqlSession, String category_id) {
       return sqlSession.selectOne("itemCategory.countChildren",category_id);
    }


}
