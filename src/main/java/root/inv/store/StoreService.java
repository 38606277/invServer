package root.inv.store;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.service.DictService;
import root.report.sys.SysContext;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreService {

    private static Logger log = Logger.getLogger(DictService.class);

    public List<Map<String,Object>> getStoreListByPage(Map<String,Object> params){

        int id = SysContext.getId();//用户的表id

        params.put("create_by",id);

        //调拨单
        if("transfer".equals(params.get("bill_type"))){
            if("transferIn".equals(params.get("sub_type"))){
                params.put("target_operator",id);
            }else{
                params.put("operator",id);
            }
        }

        List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
        try
        {
            resultList = DbSession.selectList("inv_store.getStoreListByPage",params);
            return resultList;
        }catch (Exception ex){

            throw  ex;
        }
    }

    public int getStoreListByPageCount(Map<String,Object> params){
       return DbSession.selectOne("inv_store.getStoreListByPageCount",params);
    }


    public Map<String,Object> getStoreById(Map<String,Object> params){
        return DbSession.selectOne("inv_store.getStoreById",params);
    }

    public long createStore(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.insert("inv_store.createStore",params);
        if(params.containsKey("bill_id")){
            return  Long.parseLong(params.get("bill_id").toString());
        }
        return  -1;
    }

    public boolean updateStoreById(SqlSession sqlSession,JSONObject params){
       int number =  sqlSession.update("inv_store.updateStoreById",params);
        return  0 < number;
    }


    public void deleteStoreByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("inv_store.deleteStoreByIds",map);
    }

    public void updateStoreStatusByIds(SqlSession sqlSession,String ids,String status){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        map.put("bill_status",status);
        sqlSession.update("inv_store.updateStoreStatusByIds",map);
    }


}
