package root.inv.task;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.util.DateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FndTaskService {

    private static Logger log = Logger.getLogger(FndTaskService.class);

    public  Map<String,Object> getFndTaskListByPage(Map<String,Object> map){
        Map<String,Object> map1=new HashMap<>();

        try {
            RowBounds bounds = null;
            if (map == null) {
                bounds = RowBounds.DEFAULT;
            } else {
                Integer startIndex = Integer.parseInt(map.get("pageNum").toString());
                Integer perPage = Integer.parseInt(String.valueOf(map.get("perPage")));
                if (startIndex == 1 || startIndex == 0) {
                    startIndex = 0;
                } else {
                    startIndex = (startIndex - 1) * perPage;
                }
                bounds = new PageRowBounds(startIndex, perPage);
            }
            List<Map<String, Object>> resultList = DbSession.selectList("fnd_task.getFndTaskListByPage", map, bounds);
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


    public long saveFndTask(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.insert("fnd_task.saveFndTask",params);
        if(params.containsKey("bank_account_id")){
            return  Long.parseLong(params.get("bank_account_id").toString());
        }
        return  -1;
    }

    public void updateFndTaskById(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.update("fnd_task.updateFndTaskById",params);
    }

    public void updateFndTaskBySourceIdAndTaskType(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.update("fnd_task.updateFndTaskBySourceIdAndTaskType",params);
    }


    public void deleteFndTaskByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("fnd_task.deleteFndTaskByIds",map);
    }

  

}
