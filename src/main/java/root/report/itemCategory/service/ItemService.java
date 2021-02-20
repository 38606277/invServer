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
public class ItemService {

    private static Logger log = Logger.getLogger(ItemService.class);

    @Autowired
    public ItemCategoryService itemCategoryService;

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

            //根据类别获取段位  segment， attribute

            String stringSql=getSqlNew(map.get("item_category_id"));
            List<Map<String, Object>> resultList = sqlSession.selectList("mdmItem.paramStringSql", stringSql, bounds);
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

    public String getSql(String categoryId){
        List<Map> segmentList = itemCategoryService.getItemCategorySegmentByCatId(categoryId);
        List<Map> attributeList = itemCategoryService.getItemCategoryAttributeByCatId(categoryId);
        StringBuffer stringSql=new StringBuffer();
        StringBuffer stringfrom=new StringBuffer();
        StringBuffer stringWhere=new StringBuffer();
        stringSql.append("SELECT item.item_id,item.item_category_id,item.item_description,item.uom,item.retail_price,item.factory_price,item.promotion_price," +
                "item.cost_price,item.image_url,itemcat.category_name ");
        if(segmentList.size()>0){
            for(int i=0;i<segmentList.size();i++){
                Map map2=segmentList.get(i);
                if(map2.get("input_mode").equals("dict")){
                    stringSql.append(",dicts"+i+".value_name as "+map2.get("segment"));
                    stringfrom.append(" ,mdm_dict_value as dicts"+i);
                    stringWhere.append(" and item."+map2.get("segment")+"=dicts"+i+".value_id");
                }else{
                    stringSql.append(",item."+map2.get("segment"));
                }
            }
        }
        if(attributeList.size()>0){
            for(int i=0;i<attributeList.size();i++){
                Map map2=attributeList.get(i);
                if(map2.get("input_mode").equals("dict")){
                    stringSql.append(",dicta"+i+".value_name as "+map2.get("attribute"));
                    stringfrom.append(" ,mdm_dict_value as dicta"+i);
                    stringWhere.append(" and item."+map2.get("attribute")+"=dicta"+i+".value_id");
                }else{
                    stringSql.append(",item."+map2.get("attribute"));
                }
            }
        }
        stringSql.append(" from  mdm_item as item,mdm_item_category as itemcat "+stringfrom+ " where 1=1 "+stringWhere +" " +
                " and item.item_category_id=itemcat.category_id and item.item_category_id="+ categoryId);
        System.out.println(stringSql);
        return stringSql.toString();
    }

    public String getSqlNew(String categoryId){
        List<Map> segmentList = itemCategoryService.getItemCategorySegmentByCatId(categoryId);
        List<Map> attributeList = itemCategoryService.getItemCategoryAttributeByCatId(categoryId);
        StringBuffer stringSql=new StringBuffer();
        stringSql.append("SELECT item.item_id,item.item_category_id,item.item_description,item.uom,item.retail_price,item.factory_price,item.promotion_price," +
                "item.cost_price,item.image_url,itemcat.category_name ");
        if(segmentList.size()>0){
            for(int i=0;i<segmentList.size();i++){
                Map map2=segmentList.get(i);
                stringSql.append(",item."+map2.get("segment"));
            }
        }
        if(attributeList.size()>0){
            for(int i=0;i<attributeList.size();i++){
                Map map2=attributeList.get(i);
                stringSql.append(",item."+map2.get("attribute"));
            }
        }
        stringSql.append(" from  mdm_item as item,mdm_item_category as itemcat where item.item_category_id=itemcat.category_id and item.item_category_id="+ categoryId);
        System.out.println(stringSql);
        return stringSql.toString();
    }
    /**
     * 功能描述: 根据JSON数据解析 对应数据，生成func_dict记录
     */
    public String saveOrUpdate(SqlSession sqlSession,JSONObject jsonObjectVal){
        Map<String,Object> map  = new HashMap<>();
        String id="";
        Integer newId= sqlSession.selectOne("mdmItem.getMaxId");
        newId = newId==null?1:newId;
        Map<String,Object> mapVal  = new HashMap<>();
        if (null == jsonObjectVal.getString("item_id") || "".equals(jsonObjectVal.getString("item_id")) || "null".equals(jsonObjectVal.getString("item_id"))) {
            mapVal.put("item_id", newId);
            mapVal.put("item_category_id", jsonObjectVal.getString("item_category_id"));
            mapVal.put("item_description", jsonObjectVal.getString("item_description"));
            mapVal.put("uom", jsonObjectVal.getString("uom"));
            mapVal.put("retail_price", jsonObjectVal.getString("retail_price"));
            mapVal.put("factory_price", jsonObjectVal.getString("factory_price"));
            mapVal.put("promotion_price", jsonObjectVal.getString("promotion_price"));
            mapVal.put("cost_price", jsonObjectVal.getString("cost_price"));
            mapVal.put("image_url", jsonObjectVal.getString("image_url"));
            mapVal.put("bar_code", jsonObjectVal.getString("bar_code"));
            mapVal.put("iot_code", jsonObjectVal.getString("iot_code"));
            for (Map.Entry<String, Object> entry : jsonObjectVal.entrySet()) {
                System.out.println("key值=" + entry.getKey());
                System.out.println("对应key值的value=" + entry.getValue());
                if(entry.getKey().equalsIgnoreCase("segment1")) {
                    mapVal.put("segment1", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment2")) {
                    mapVal.put("segment2", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment3")) {
                    mapVal.put("segment3", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment4")) {
                    mapVal.put("segment4", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment5")) {
                    mapVal.put("segment5", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment6")) {
                    mapVal.put("segment6", entry.getValue());
                }if(entry.getKey().equalsIgnoreCase("segment7")) {
                    mapVal.put("segment7", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment8")) {
                    mapVal.put("segment8", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment9")) {
                    mapVal.put("segment9", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment10")) {
                    mapVal.put("segment10", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute1")) {
                    mapVal.put("attribute1", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute2")) {
                    mapVal.put("attribute2", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute3")) {
                    mapVal.put("attribute3", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute4")) {
                    mapVal.put("attribute4", entry.getValue());
                }

                if(entry.getKey().equalsIgnoreCase("attribute5")) {
                    mapVal.put("attribute5", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute6")) {
                    mapVal.put("attribute6", entry.getValue());
                }

                if(entry.getKey().equalsIgnoreCase("attribute7")) {
                    mapVal.put("attribute7", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute8")) {
                    mapVal.put("attribute8", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute9")) {
                    mapVal.put("attribute9", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute10")) {
                    mapVal.put("attribute10", entry.getValue());
                }
            }
            sqlSession.insert("mdmItem.createMdmItem",mapVal);

            List<Map> orgList = sqlSession.selectList("inv_org.getAll");

            List<Map> insertMap=new ArrayList<Map>();
            if(orgList.size()>0){
                for(Map resmap :orgList){
                    Map onHandmap = new HashMap();
                    onHandmap.put("item_id",mapVal.get("item_id"));
                    onHandmap.put("location_id",null);
                    onHandmap.put("on_hand_quantity",null);
                    onHandmap.put("price",null);
                    onHandmap.put("amount",null);
                    onHandmap.put("min",null);
                    onHandmap.put("max",null);
                    onHandmap.put("org_id",resmap.get("org_id"));
                    insertMap.add(onHandmap);
                }
                sqlSession.insert("inv_item_on_hand.saveItemOnHandAll",insertMap);
            }
            newId++;
        } else {
            mapVal.put("item_id", jsonObjectVal.getString("item_id"));
            id = jsonObjectVal.getString("item_id");
            mapVal.put("item_category_id", jsonObjectVal.getString("item_category_id"));
            mapVal.put("item_description", jsonObjectVal.getString("item_description"));
            mapVal.put("uom", jsonObjectVal.getString("uom"));
            mapVal.put("retail_price", jsonObjectVal.getString("retail_price"));
            mapVal.put("factory_price", jsonObjectVal.getString("factory_price"));
            mapVal.put("promotion_price", jsonObjectVal.getString("promotion_price"));
            mapVal.put("cost_price", jsonObjectVal.getString("cost_price"));
            mapVal.put("image_url", jsonObjectVal.getString("image_url"));
            mapVal.put("bar_code", jsonObjectVal.getString("bar_code"));
            mapVal.put("iot_code", jsonObjectVal.getString("iot_code"));
            for (Map.Entry<String, Object> entry : jsonObjectVal.entrySet()) {
                System.out.println("key值=" + entry.getKey());
                System.out.println("对应key值的value=" + entry.getValue());
                if(entry.getKey().equalsIgnoreCase("segment1")) {
                    mapVal.put("segment1", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment2")) {
                    mapVal.put("segment2", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment3")) {
                    mapVal.put("segment3", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment4")) {
                    mapVal.put("segment4", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment5")) {
                    mapVal.put("segment5", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment6")) {
                    mapVal.put("segment6", entry.getValue());
                }if(entry.getKey().equalsIgnoreCase("segment7")) {
                    mapVal.put("segment7", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment8")) {
                    mapVal.put("segment8", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment9")) {
                    mapVal.put("segment9", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("segment10")) {
                    mapVal.put("segment10", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute1")) {
                    mapVal.put("attribute1", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute2")) {
                    mapVal.put("attribute2", entry.getValue());
                }

                if(entry.getKey().equalsIgnoreCase("attribute3")) {
                    mapVal.put("attribute3", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute4")) {
                    mapVal.put("attribute4", entry.getValue());
                }

                if(entry.getKey().equalsIgnoreCase("attribute5")) {
                    mapVal.put("attribute5", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute6")) {
                    mapVal.put("attribute6", entry.getValue());
                }

                if(entry.getKey().equalsIgnoreCase("attribute7")) {
                    mapVal.put("attribute7", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute8")) {
                    mapVal.put("attribute8", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute9")) {
                    mapVal.put("attribute9", entry.getValue());
                }
                if(entry.getKey().equalsIgnoreCase("attribute10")) {
                    mapVal.put("attribute10", entry.getValue());
                }

            }
            sqlSession.insert("mdmItem.updateMdmItem", mapVal);
        }

        return id;
    }


    public String saveOrUpdateBatch(SqlSession sqlSession,JSONObject jsonObjectVal) {
        Map<String, Object> map = new HashMap<>();
        String id = "";
        Integer newId = sqlSession.selectOne("mdmItem.getMaxId");
        newId = newId == null ? 1 : newId;
        Map<String, Object> mapVal = new HashMap<>();
        if (null == jsonObjectVal.getString("item_id") || "".equals(jsonObjectVal.getString("item_id")) || "null".equals(jsonObjectVal.getString("item_id"))) {
           JSONArray list=jsonObjectVal.getJSONArray("columnSkeyList");
            for (int i = 0; i < list.size(); i++) {
                if (null != list.get(i) && !"".equals(list.get(i))) {
                    JSONObject objss= (JSONObject) list.get(i);
                    for (Map.Entry<String, Object> entry : objss.entrySet()) {
                        Object vale = entry.getValue();
                        String vkey = entry.getKey();
                        jsonObjectVal.put(vkey, vale);
                    }
                    mapVal.put("item_id", newId);
                    mapVal.put("item_category_id", jsonObjectVal.getString("item_category_id"));
                    mapVal.put("item_description", jsonObjectVal.getString("item_description"));
                    mapVal.put("uom", jsonObjectVal.getString("uom"));
                    mapVal.put("retail_price", jsonObjectVal.getString("retail_price"));
                    mapVal.put("factory_price", jsonObjectVal.getString("factory_price"));
                    mapVal.put("promotion_price", jsonObjectVal.getString("promotion_price"));
                    mapVal.put("cost_price", jsonObjectVal.getString("cost_price"));
                    mapVal.put("image_url", jsonObjectVal.getString("image_url"));
                    mapVal.put("bar_code", jsonObjectVal.getString("bar_code"));
                    mapVal.put("iot_code", jsonObjectVal.getString("iot_code"));
                    for (Map.Entry<String, Object> entry : jsonObjectVal.entrySet()) {
                        System.out.println("key值=" + entry.getKey());
                        System.out.println("对应key值的value=" + entry.getValue());
                        if (entry.getKey().equalsIgnoreCase("segment1")) {
                            mapVal.put("segment1", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment2")) {
                            mapVal.put("segment2", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment3")) {
                            mapVal.put("segment3", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment4")) {
                            mapVal.put("segment4", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment5")) {
                            mapVal.put("segment5", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment6")) {
                            mapVal.put("segment6", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment7")) {
                            mapVal.put("segment7", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment8")) {
                            mapVal.put("segment8", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment9")) {
                            mapVal.put("segment9", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("segment10")) {
                            mapVal.put("segment10", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("attribute1")) {
                            mapVal.put("attribute1", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("attribute2")) {
                            mapVal.put("attribute2", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("attribute3")) {
                            mapVal.put("attribute3", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("attribute4")) {
                            mapVal.put("attribute4", entry.getValue());
                        }

                        if (entry.getKey().equalsIgnoreCase("attribute5")) {
                            mapVal.put("attribute5", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("attribute6")) {
                            mapVal.put("attribute6", entry.getValue());
                        }

                        if (entry.getKey().equalsIgnoreCase("attribute7")) {
                            mapVal.put("attribute7", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("attribute8")) {
                            mapVal.put("attribute8", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("attribute9")) {
                            mapVal.put("attribute9", entry.getValue());
                        }
                        if (entry.getKey().equalsIgnoreCase("attribute10")) {
                            mapVal.put("attribute10", entry.getValue());
                        }
                    }
                    sqlSession.insert("mdmItem.createMdmItem", mapVal);

                    List<Map> orgList = sqlSession.selectList("inv_org.getAll");

                    List<Map> insertMap = new ArrayList<Map>();
                    if (orgList.size() > 0) {
                        for (Map resmap : orgList) {
                            Map onHandmap = new HashMap();
                            onHandmap.put("item_id", mapVal.get("item_id"));
                            onHandmap.put("location_id", null);
                            onHandmap.put("on_hand_quantity", null);
                            onHandmap.put("price", null);
                            onHandmap.put("amount", null);
                            onHandmap.put("min", null);
                            onHandmap.put("max", null);
                            onHandmap.put("org_id", resmap.get("org_id"));
                            insertMap.add(onHandmap);
                        }
                        sqlSession.insert("inv_item_on_hand.saveItemOnHandAll", insertMap);
                    }
                    newId++;
                }

            }

        }
        return null;
    }

    public Map getItemByItemId(Map<String, String> map) {
        Map maps=DbFactory.Open(DbFactory.FORM).selectOne("mdmItem.getItemByItemId",map);
        return maps;
    }

    public Map getAllPageByCategoryId(Map<String, String> map) {
        Map resmap =new HashMap();
        List<Map> list= DbFactory.Open(DbFactory.FORM).selectList("mdmItem.getAllPageByCategoryId",map);
        Map paramMap= new HashMap();
        paramMap.put("category_id",map.get("item_category_id"));
        Map maps=itemCategoryService.getItemCategoryById(paramMap);
        resmap.put("mainForm",maps);
        resmap.put("lineForm",list);
        return resmap;
    }

    public void deleteItemByID(SqlSession sqlSession, String itemid) {
        sqlSession.delete("mdmItem.deleteItemByID",itemid);
        sqlSession.delete("inv_item_on_hand.deleteOnHandItemByID",Integer.parseInt(itemid));
    }
}
