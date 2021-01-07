package root.inv.store;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.db.DbFactory;
import root.report.db.DbManager;
import root.report.service.DictService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StoreService {

    private static Logger log = Logger.getLogger(DictService.class);

    public List<Map<String,Object>> getStoreListByPage(Map<String,Object> params){
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


    public long createStore(Map<String,Object> params){
        DbSession.insert("inv_store.createStore",params);
        if(params.containsKey("transaction_id")){
            return (long) params.get("transaction_id");
        }
        return  -1;
    }

}
