package root.inv.po;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.service.DictService;
import root.report.sys.SysContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PoHeadersService {

    private static Logger log = Logger.getLogger(PoHeadersService.class);

    public List<Map<String,Object>> getPoHeadersListByPage(Map<String,Object> params){
        int id = SysContext.getId();//id
        params.put("create_by",id);

        List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
        try
        {
            resultList = DbSession.selectList("po_headers.getPoHeaderListByPage",params);
            return resultList;
        }catch (Exception ex){

            throw  ex;
        }
    }

    public int getPoHeadersListByPageCount(Map<String,Object> params){
        return DbSession.selectOne("po_headers.getPoHeaderListByPageCount",params);
    }


    public Map<String,Object> getPoHeadersById(Map<String,Object> params){
        return DbSession.selectOne("po_headers.getPoHeaderById",params);
    }

    public long savePoHeaders(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.insert("po_headers.savePoHeader",params);
        if(params.containsKey("po_header_id")){
            return  Long.parseLong(params.get("po_header_id").toString());
        }
        return  -1;
    }

    public void updatePoHeadersById(SqlSession sqlSession,JSONObject params){
        sqlSession.update("po_headers.updatePoHeaderById",params);
    }


    public void deletePoHeadersByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("po_headers.deletePoHeaderByIds",map);
    }

    public void updatePoHeadersStatusByIds(SqlSession sqlSession,String ids,String status){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        map.put("status",status);
        sqlSession.update("po_headers.updatePoHeaderStatusByIds",map);
    }

}
