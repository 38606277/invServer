package root.inv.bom;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.sys.SysContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BomHeadersService {
    
    public static List<Map<String,Object>> getBomHeadersListByPage(Map<String, Object> params){
        int id = SysContext.getId();//id
        params.put("create_by",id);

        List<Map<String,Object>> resultList = new ArrayList<>();
        try
        {
            resultList = DbSession.selectList("bom_header.getBomHeaderListByPage",params);
            return resultList;
        }catch (Exception ex){

            throw  ex;
        }
    }

    public int getBomHeadersListByPageCount(Map<String,Object> params){
        return DbSession.selectOne("bom_header.getBomHeaderListByPageCount",params);
    }


    public Map<String,Object> getBomHeadersById(Map<String,Object> params){
        return DbSession.selectOne("bom_header.getBomHeaderById",params);
    }

    public long saveBomHeaders(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.insert("bom_header.saveBomHeader",params);
        if(params.containsKey("item_id")){
            return  Long.parseLong(params.get("item_id").toString());
        }
        return  -1;
    }

    public void updateBomHeadersById(SqlSession sqlSession,JSONObject params){
        sqlSession.update("bom_header.updateBomHeaderById",params);
    }


    public void deleteBomHeadersByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("bom_header.deleteBomHeaderByIds",map);
    }
}
