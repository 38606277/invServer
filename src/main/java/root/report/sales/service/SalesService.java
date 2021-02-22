package root.report.sales.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.report.db.DbFactory;
import root.report.itemCategory.service.ItemCategoryService;

import java.util.*;

@Service
public class SalesService {

    private static Logger log = Logger.getLogger(SalesService.class);

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
            List<Map<String, Object>> resultList = sqlSession.selectList("mdmItem.getAllPageForSales", map, bounds);
            Long totalSize = 0L;
            if (map != null && map.size() != 0) {
                totalSize = ((PageRowBounds) bounds).getTotal();
            } else {
                totalSize = Long.valueOf(resultList.size());
            }
            Set<String> itemcate = new HashSet<>();
            Map<String,Object> ppmap = new HashMap();
            for(int i=0;i<resultList.size();i++) {
                Map tempmap = resultList.get(i);
                String mkeyRes="", skeyRes="";
                List<Map> mlist=null, slist=null;
                String catid=tempmap.get("item_category_id").toString();
                if(!itemcate.contains(catid)) {
                    Map msmap = new HashMap();
                    mlist= itemCategoryService.getItemCategorySegmentByCatIdAndKey(catid,"mkey");
                    slist= itemCategoryService.getItemCategorySegmentByCatIdAndKey(catid,"skey");
                    msmap.put("mkey",mlist);
                    msmap.put("skey",slist);
                    ppmap.put(catid,msmap);
                    itemcate.add(catid);
                }else{
                    Map m= (Map) ppmap.get(catid);
                    mlist = (List<Map>) m.get("mkey");
                    slist = (List<Map>) m.get("skey");
                }
                if(null!=mlist) {
                    for (int ii = 0; ii < mlist.size(); ii++) {
                        String segment = mlist.get(ii).get("segment").toString();
                        mkeyRes = tempmap.get(segment)+ " " + mkeyRes;
                    }
                }
                if(null!=slist) {
                    for (int iii = 0; iii < slist.size(); iii++) {
                        String segment = slist.get(iii).get("segment").toString();
                        skeyRes = tempmap.get(segment)+ " " + skeyRes;
                    }
                }
                tempmap.put("mkeyRes",mkeyRes);
                tempmap.put("skeyRes",skeyRes);
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
    public Map saveOrUpdateShipment(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> resmap  = new HashMap<>();
        resmap.put("status",true);
        resmap.put("info","保存成功");
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("vendor_code", jsonObject.getString("vendor_code"));
            map.put("vendor_id", jsonObject.getString("vendor_id"));
            Integer count = sqlSession.selectOne("shipment.isExit", map);
            if (count == 0) {
                map.put("vendor_name", jsonObject.getString("vendor_name"));
                map.put("address", jsonObject.getString("address"));
               if (null == jsonObject.getString("vendor_id") || "".equals(jsonObject.getString("vendor_id"))) {
                    Integer newId = sqlSession.selectOne("shipment.getMaxId");
                    newId = newId == null ? 1 : newId;
                    map.put("vendor_id", newId);
                    sqlSession.insert("shipment.createShipment", map);
                } else {
                    map.put("vendor_id", jsonObject.getString("vendor_id"));
                    sqlSession.update("shipment.updateShipment", map);
                }
            } else {
                resmap.put("status", false);
                resmap.put("info", "名称已存在");
            }
        }catch (Exception e){
            resmap.put("status", false);
            resmap.put("info", "保存失败");
            e.printStackTrace();
        }
        return resmap;
    }




    public void deleteShipmentById(SqlSession sqlSession,String vendor_id){
        Map<String,Object> map=new HashMap();
        map.put("vendor_id",vendor_id);
        sqlSession.delete("shipment.deleteShipmentById",map);
    }

    public Map getShipmentByID(Map m) {
        return DbFactory.Open(DbFactory.FORM).selectOne("shipment.getShipmentByID",m);
    }

    public List<Map> getOrgAll() {
        return DbFactory.Open(DbFactory.FORM).selectList("retail_sales.getOrgAll");
    }
}
