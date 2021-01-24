package root.report.sales.service;

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
public class SalesService {

    private static Logger log = Logger.getLogger(SalesService.class);

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
            List<Map<String, Object>> resultList = sqlSession.selectList("shipment.getAllPage", map, bounds);
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

}
