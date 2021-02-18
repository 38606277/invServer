package root.report.sales.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.db.DbFactory;
import root.report.service.DictService;
import root.report.sys.SysContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WholeSaleService {

    private static Logger log = Logger.getLogger(WholeSaleService.class);

    public Map<String,Object> getStoreById(Map<String,Object> params){
        return DbSession.selectOne("whole_sale_header.getStoreById",params);
    }

    public long createStore(SqlSession sqlSession,Map<String,Object> params){
        String billCode = DbSession.selectOne("fnd_order_number_setting.getOrderNumber",params.get("bill_type"));
        params.put("bill_code",billCode);
        sqlSession.insert("whole_sale_header.createStore",params);
        if(params.containsKey("bill_id")){
            return  Long.parseLong(params.get("bill_id").toString());
        }
        return  -1;
    }

    public boolean updateStoreById(SqlSession sqlSession,JSONObject params){
       int number =  sqlSession.update("whole_sale_header.updateStoreById",params);
        return  0 < number;
    }


    public void deleteStoreByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("whole_sale_header.deleteStoreByIds",map);
    }

    public void updateStoreStatusByIds(SqlSession sqlSession,String ids,int status){
        Map<String,Object> map = new HashMap<>();
        map.put("ids",ids);
        map.put("bill_status",status);
        sqlSession.update("whole_sale_header.updateStoreStatusByIds",map);
    }


    public Map<String, Object> getWholeSaleListByPage(Map<String,Object> pJson) {
        Map<String,Object> map1=new HashMap<>();

        try {
            RowBounds bounds = null;
            if (pJson == null) {
                bounds = RowBounds.DEFAULT;
            } else {
                int currentPage = Integer.valueOf(pJson.get("pageNum").toString());
                int perPage = Integer.valueOf(pJson.get("perPage").toString());
                if (1 == currentPage || 0 == currentPage) {
                    currentPage = 0;
                } else {
                    currentPage = (currentPage - 1) * perPage;
                }

                pJson.put("startIndex",currentPage);
                pJson.put("perPage",perPage);
                bounds = new PageRowBounds(currentPage, perPage);
            }
            int id = SysContext.getId();//用户的表id
            pJson.put("create_by",id);
            List<Map<String, Object>> resultList = DbSession.selectList("whole_sale_header.getStoreListByPage", pJson, bounds);
            Long totalSize = 0L;
            if (pJson != null && pJson.size() != 0) {
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
}
