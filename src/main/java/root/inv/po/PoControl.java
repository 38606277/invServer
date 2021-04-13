package root.inv.po;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import root.inv.approval.ApprovalRuleService;
import root.inv.task.FndTaskService;
import root.report.common.DbSession;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.itemCategory.service.ItemCategoryService;
import root.report.sys.SysContext;
import root.report.util.DateUtil;
import root.report.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购订单
 */
@RestController
@RequestMapping(value = "/reportServer/po")
public class PoControl extends RO {

    @Autowired
    private PoHeadersService poHeadersService;

    @Autowired
    private PoLinesService poLinesService;

    @Autowired
    private ItemCategoryService itemCategoryService;

    @Autowired
    private FndTaskService fndTaskService;

    @Autowired
    private ApprovalRuleService approvalRuleService;

    //查询所有订单
    @RequestMapping(value = "/getPoListByPage", produces = "text/plain;charset=UTF-8")
    public String getPoListByPage(@RequestBody JSONObject pJson) {
        Map<String,Object> result = poHeadersService.getPoHeadersListByPage(pJson);
        return SuccessMsg("查询成功",result);
    }

    //查询详情
    @RequestMapping(value = "/getPoById", produces = "text/plain;charset=UTF-8")
    public String getPoById(@RequestBody JSONObject pJson){

        Map<String, Object> mainData = poHeadersService.getPoHeadersById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        String headId  = String.valueOf(mainData.get("po_header_id"));
        List<Map<String,Object>> lines =  poLinesService.getPoLinesByHeaderId(headId);
        Map<String,Object> result = new HashMap<>();
        result.put("mainData",mainData);

        Map<String,List<Map>> columnMap = new HashMap<>();
        Map<String,List<Map>> lineDateMap = new HashMap<>();
        Map<String,String> categoryNameMap = new HashMap<>();
        for(Map<String,Object> map : lines){
            String itemCategoryId = String.valueOf(map.get("item_category_id"));
            String itemCategoryName = String.valueOf(map.get("category_name"));
            if(!columnMap.containsKey(itemCategoryId)){
                Map<String,Object> params = new HashMap<>();
                params.put("category_id",itemCategoryId);

                //动态列
                List<Map> columnList = itemCategoryService.getItemCategorySegmentByPid(params);
                for(Map columnItemMap :columnList ){
                    columnItemMap.put("title",columnItemMap.get("segment_name"));
                    columnItemMap.put("dataIndex",columnItemMap.get("segment"));
                }
                columnMap.put(itemCategoryId,columnList);
                categoryNameMap.put(itemCategoryId,itemCategoryName);
                //行数据
                List<Map> lineList = new ArrayList<>();
                lineList.add(map);
                lineDateMap.put(itemCategoryId,lineList);
            }else{
                List<Map> lineList =  lineDateMap.get(itemCategoryId);
                lineList.add(map);
            }
        }

        JSONArray jsonArray = new JSONArray();
        for(String key : columnMap.keySet()){
            List<Map> column  = columnMap.get(key);
            List<Map> dataList = lineDateMap.get(key);
            String categoryName =  categoryNameMap.get(key);
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("columnList",column);
            jsonObject.put("dataList",dataList);
            jsonObject.put("categoryName",categoryName);
            jsonObject.put("categoryId",key);
            jsonArray.add(jsonObject);
        }

        result.put("linesData",jsonArray);
        return SuccessMsg("获取成功", result);



    }

    @RequestMapping(value = "/getPoLinesById", produces = "text/plain;charset=UTF-8")
    public String getPoListById(@RequestBody JSONObject pJson){
        String headId = String.valueOf(pJson.get("po_header_id"));
        List<Map<String,Object>> lines =  poLinesService.getPoLinesByHeaderId(headId);
        return SuccessMsg(lines, lines.size());
    }

    @RequestMapping(value = "/getPoLinesColumnById", produces = "text/plain;charset=UTF-8")
    public String getPoLinesColumnById(@RequestBody JSONObject pJson){
        String headId = String.valueOf(pJson.get("po_header_id"));
        List<Map<String,Object>> lines =  poLinesService.getPoLinesByHeaderId(headId);
        Map<String,List<Map>> columnMap = new HashMap<>();
        Map<String,String> categoryNameMap = new HashMap<>();
        for(Map<String,Object> map : lines){
            String itemCategoryId = String.valueOf(map.get("item_category_id"));
            String itemCategoryName = String.valueOf(map.get("category_name"));
            if(!columnMap.containsKey(itemCategoryId)){
                Map<String,Object> params = new HashMap<>();
                params.put("category_id",itemCategoryId);

                //动态列
                List<Map> columnList = itemCategoryService.getItemCategorySegmentByPid(params);
                for(Map columnItemMap :columnList ){
                    columnItemMap.put("title",columnItemMap.get("segment_name"));
                    columnItemMap.put("dataIndex",columnItemMap.get("segment"));
                }
                columnMap.put(itemCategoryId,columnList);
                categoryNameMap.put(itemCategoryId,itemCategoryName);
                //行数据
                List<Map> lineList = new ArrayList<>();
                lineList.add(map);
            }
        }

        JSONArray jsonArray = new JSONArray();
        for(String key : columnMap.keySet()){
            List<Map> column  = columnMap.get(key);
            String categoryName =  categoryNameMap.get(key);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("columnList",column);
            jsonObject.put("categoryName",categoryName);
            jsonObject.put("categoryId",key);
            jsonArray.add(jsonObject);
        }

        return SuccessMsg("查询成功",jsonArray);
    }

    //更新
    @RequestMapping(value = "/updatePoById", produces = "text/plain;charset=UTF-8")
    public String updatePoById(@RequestBody JSONObject pJson)throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        int userId = SysContext.getId();
        try {
            sqlSession.getConnection().setAutoCommit(false);

            //更新主实体
            JSONObject mainData = pJson.getJSONObject("mainData");

            poHeadersService.updatePoHeadersById(sqlSession,mainData);

            //删除行数据
            String deleteIds  = pJson.getString("deleteData");
            if(deleteIds!=null && !deleteIds.isEmpty()){
                poLinesService.deletePoLines(sqlSession,deleteIds);
            }

            //新增或更新行数据
            JSONArray jsonArray = pJson.getJSONArray("linesData");
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put("line_number",i);
                jsonObject.put("create_by",userId);
                jsonObject.put("header_id", mainData.get("po_header_id"));
            }
            poLinesService.saveOrUpdatePoLinesList(sqlSession,jsonArray);

            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",mainData.get("po_header_id"));
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


    /**
     * 新增事物
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/createPo", produces = "text/plain; charset=utf-8")
    public String createPo(@RequestBody JSONObject pJson) throws SQLException{
        int userId = SysContext.getId();

        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try {

            sqlSession.getConnection().setAutoCommit(false);

            JSONObject mainData = pJson.getJSONObject("mainData");
            mainData.put("create_by",userId);
            mainData.put("update_by",userId);
            mainData.put("create_date", DateUtil.getCurrentTimm());
            mainData.put("update_date", DateUtil.getCurrentTimm());

            String billCode = sqlSession.selectOne("fnd_order_number_setting.getOrderNumber","po_order");
            mainData.put("header_code", billCode);

            //保存主数据
            long id = poHeadersService.savePoHeaders(sqlSession,mainData);

            if(id < 0){
                return ErrorMsg("2000","创建失败");
            }

            JSONArray jsonArray = pJson.getJSONArray("linesData");
            if(jsonArray !=null && 0 <jsonArray.size()){
                for(int i = 0; i < jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    jsonObject.put("line_number",i);
                    jsonObject.put("create_by",userId);
                    jsonObject.put("header_id",id);
                    jsonObject.put("rcv_quantity",0);
                    jsonObject.put("cancel_flag",0);
                }
                poLinesService.insertPoLinesAll(sqlSession,jsonArray);
            }

            //状态为1表示提交
             if( pJson.containsKey("status") &&  "1".equals(pJson.get("status"))){
                 //获取审批人
                 long assignerId =  approvalRuleService.getApprovalUser(String.valueOf(userId),"po");
                 if(assignerId < 0){
                     return ErrorMsg("提交失败","审批人未配置");
                 }

                 fndTaskService.savaTask(sqlSession,"po","po",id,userId,assignerId);
             }
            sqlSession.getConnection().commit();
            return SuccessMsg("创建成功",id);
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

    @RequestMapping(value = "/deletePoByIds", produces = "text/plain;charset=UTF-8")
    public String deletePoByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //更新主实体
            poHeadersService.deletePoHeadersByIds(sqlSession,deleteIds);
            //删除行数据
            poLinesService.deletePoLinesByHeaderIds(sqlSession,deleteIds);
            sqlSession.getConnection().commit();
            return SuccessMsg("删除成功","");
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

    @RequestMapping(value = "/updatePoStatusByIds", produces = "text/plain;charset=UTF-8")
    public String updatePoStatusByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        String deleteIds  = pJson.getString("ids");
        String status = pJson.getString("bill_status");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("处理失败","请选择处理项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //批量过账
            poHeadersService.updatePoHeadersStatusByIds(sqlSession,deleteIds,status);
            sqlSession.getConnection().commit();
            return SuccessMsg("处理成功","");
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


    @RequestMapping(value = "/updatePoStatusById", produces = "text/plain;charset=UTF-8")
    public String updatePoStatusById(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String taskType = "po";

        if(!pJson.containsKey("bill_id")){
            return ErrorMsg("提交失败","id不存在或已删除");
        }

        int updateId  = pJson.getInteger("bill_id");
        String status = pJson.getString("bill_status");
        int userId = SysContext.getId();

        if("1".equals(status)){ //提交


            //获取审批人
            long assignerId =  approvalRuleService.getApprovalUser(String.valueOf(userId),taskType);
            if(assignerId < 0){
                return ErrorMsg("提交失败","审批人未配置");
            }

            Map<String,Object> updateMap = new HashMap<>();
            updateMap.put("po_header_id",updateId);
            updateMap.put("approval_id",assignerId);
            poHeadersService.updatePoHeadersById(sqlSession,updateMap);//更新审批人

            fndTaskService.savaTask(sqlSession,taskType,taskType,updateId,userId,assignerId);

        }else if("2".equals(status)){ //审批
            //更新任务,将待办任务修改为完成状态
            fndTaskService.completeTask(sqlSession,taskType,updateId);
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //批量过账
            poHeadersService.updatePoHeadersStatusByIds(sqlSession,String.valueOf(updateId),status);
            sqlSession.getConnection().commit();
            return SuccessMsg("处理成功","");
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

}
