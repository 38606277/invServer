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

    public long savaTask(SqlSession sqlSession,String taskType,String billType,long billId,long ownerId,long assignerId){
        String billTypeName = null;
        String func_url = null;
        if("store_other".equals(billType)){ //其他入库
            billTypeName = "其他入库";
            func_url = "/transation/store/other/edit/" + billId;
        } else if("deliver_other".equals(billType)){//出库
            billTypeName= "其他出库";
            func_url = "/transation/deliver/other/edit/" + billId;
        }else if("transfer".equals(billType)){
            billTypeName = "调拨出库";
            func_url = "/transation/transfer/out/edit/" + billId;
        }else if("deliver_sales".equals(billType)){//销售出库
            billTypeName= "销售出库";
            func_url = "/sales/sales/edit/" + billId;
        }else if("deliver_wholesales".equals(billType)){//批发出库
            billTypeName = "批发出库";
            func_url = "/sales/sales/edit/" + billId;
        }else if("count".equals(billType)){//盘点
            billTypeName = "盘点";
            func_url = "/transation/count/edit/" + billId;
        } else if ("po".equals(billType)) {
            billTypeName = "采购订单";
            func_url = "/order/po/edit/" + billId;

        }else if("pd".equals(billType)){
            billTypeName = "生产订单";
            func_url = "/order/pd/edit/" + billId;
        }

        if(billTypeName!=null) {
            HashMap taskMap = new HashMap<String, Object>();
            taskMap.put("task_name", "您有一条新的" + billTypeName + "待办");
            taskMap.put("owner_id", ownerId);
            taskMap.put("assigner_id", assignerId);
            taskMap.put("assign_date", DateUtil.getCurrentTimm());
            taskMap.put("receive_date", null);
            taskMap.put("last_update_date", DateUtil.getCurrentTimm());
            taskMap.put("complete_date", null);
            taskMap.put("task_status", 0);
            taskMap.put("task_type", taskType);
            taskMap.put("func_url", func_url);
            taskMap.put("func_param", null);
            taskMap.put("task_description", billTypeName);
            taskMap.put("task_level", 0);
            taskMap.put("source_id", billId);
            return saveFndTask(sqlSession,taskMap);
        }
        return -1;
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

    /**
     * 完成待办任务
     * @param sqlSession
     * @param taskType 待办类别
     * @param billId 单据id
     */
    public void completeTask(SqlSession sqlSession,String taskType,long billId){
        HashMap taskMap = new HashMap<String,Object>();
        taskMap.put("last_update_date",DateUtil.getCurrentTimm());
        taskMap.put("complete_date",DateUtil.getCurrentTimm());
        taskMap.put("task_status",2);
        taskMap.put("task_type",taskType);
        taskMap.put("source_id",billId);
        updateFndTaskBySourceIdAndTaskType(sqlSession,taskMap);
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
