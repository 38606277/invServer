package root.inv.pd;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.sys.SysContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生产订单
 */
@Service
public class PdOrderHeaderService {
    
    public  List<Map<String,Object>> getPdOrderHeaderListByPage(Map<String, Object> params){
        int id = SysContext.getId();//id
        params.put("create_by",id);

        List<Map<String,Object>> resultList = new ArrayList<>();
        try
        {
            resultList = DbSession.selectList("pd_order_header.getPdOrderHeaderListByPage",params);
            return resultList;
        }catch (Exception ex){

            throw  ex;
        }
    }

    public int getPdOrderHeaderListByPageCount(Map<String,Object> params){
        return DbSession.selectOne("pd_order_header.getPdOrderHeaderListByPageCount",params);
    }


    public Map<String,Object> getPdOrderHeaderById(Map<String,Object> params){
        return DbSession.selectOne("pd_order_header.getPdOrderHeaderById",params);
    }

    public long savePdOrderHeader(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.insert("pd_order_header.savePdOrderHeader",params);
        if(params.containsKey("pd_header_id")){
            return  Long.parseLong(params.get("pd_header_id").toString());
        }
        return  -1;
    }

    public void updatePdOrderHeaderById(SqlSession sqlSession,JSONObject params){
        sqlSession.update("pd_order_header.updatePdOrderHeaderById",params);
    }


    public void deletePdOrderHeaderByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("pd_order_header.deletePdOrderHeaderByIds",map);
    }
}
