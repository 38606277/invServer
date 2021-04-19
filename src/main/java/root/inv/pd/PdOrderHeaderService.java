package root.inv.pd;


import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import root.inv.BaseService;
import root.report.common.DbSession;
import root.report.sys.SysContext;
import java.util.HashMap;
import java.util.Map;

/**
 * 生产订单
 */
@Service
public class PdOrderHeaderService extends BaseService {

    public  Map<String,Object> getPdOrderHeaderListByPage(Map<String, Object> params){
        int id = SysContext.getId();//id
        params.put("create_by",id);
        params.put("approval_id",id);
        return getDataListByPage("pd_order_header.getPdOrderHeaderListByPage",params);
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

    public void updatePdOrderHeaderById(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.update("pd_order_header.updatePdOrderHeaderById",params);
    }

    public void updatePdHeadersStatusByIds(SqlSession sqlSession,String ids,String status){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        map.put("status",status);
        sqlSession.update("pd_order_header.updatePdOrderHeaderStatusByIds",map);
    }

    public void deletePdOrderHeaderByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.delete("pd_order_header.deletePdOrderHeaderByIds",map);
    }
}
