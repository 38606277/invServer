package root.report.customers.service;

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
public class CustomersService {

    private static Logger log = Logger.getLogger(CustomersService.class);


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
            List<Map<String, Object>> resultList = sqlSession.selectList("customers.getAllPage", map, bounds);
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
    public Map saveOrUpdateCustomers(SqlSession sqlSession,JSONObject jsonObject){
        Map<String,Object> resmap  = new HashMap<>();
        resmap.put("status",true);
        resmap.put("info","保存成功");
        try {


            Map<String, Object> map = new HashMap<>();
            map.put("customer_name", jsonObject.getString("customer_name"));
            map.put("customer_id", jsonObject.getString("customer_id"));
            Integer count = sqlSession.selectOne("customers.isExit", map);
            if (count == 0) {
                map.put("customer_address", jsonObject.getString("customer_address"));
                map.put("customer_bank", jsonObject.getString("customer_bank"));
                map.put("customer_link", jsonObject.getString("customer_link"));
                map.put("customer_type", jsonObject.getString("customer_type"));
                map.put("area_id", jsonObject.getString("area_id").equals("") ? null : jsonObject.getString("area_id"));
                map.put("bank_name", jsonObject.getString("bank_name"));
                map.put("bank_account_num", jsonObject.getString("bank_account_num"));
                if (null == jsonObject.getString("customer_id") || "".equals(jsonObject.getString("customer_id"))) {
                    Integer newId = sqlSession.selectOne("customers.getMaxId");
                    newId = newId == null ? 1 : newId;
                    map.put("customer_id", newId);
                    sqlSession.insert("customers.createCustomers", map);
                } else {
                    map.put("customer_id", jsonObject.getString("customer_id"));
                    sqlSession.update("customers.updateCustomers", map);
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




    public void deleteCustomersById(SqlSession sqlSession,String customer_id){
        Map<String,Object> map=new HashMap();
        map.put("customer_id",customer_id);
        sqlSession.delete("customers.deleteCustomersById",map);
    }

    public Map getCustomersByID(Map m) {
        return DbFactory.Open(DbFactory.FORM).selectOne("customers.getCustomersByID",m);
    }

}
