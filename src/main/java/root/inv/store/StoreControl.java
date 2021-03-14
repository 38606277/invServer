package root.inv.store;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.inv.onhand.InvItemOnHandService;
import root.inv.pd.PdOrderLinesService;
import root.inv.po.PoLinesService;
import root.inv.task.FndTaskService;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.itemCategory.service.ItemCategoryService;
import root.report.sys.SysContext;
import root.report.util.DateUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入库
 */
@RestController
@RequestMapping(value = "/reportServer/invStore")
public class StoreControl extends RO {


    @Autowired
    public StoreService storeService;

    @Autowired
    InvBillLineService invBillLineService;

    @Autowired
    InvItemTransactionService invItemTransactionService;

    @Autowired
    PoLinesService poLinesService;

    @Autowired
    FndTaskService fndTaskService;

    @Autowired
    public ItemCategoryService itemCategoryService;

    @Autowired
    public InvItemOnHandService invItemOnHandService;



    //查询所有事物
    @RequestMapping(value = "/getStoreListByPage", produces = "text/plain;charset=UTF-8")
    public String getStoreListByPage(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }

        pJson.put("startIndex",currentPage);
        pJson.put("perPage",perPage);

        List<Map<String, Object>> list = storeService.getStoreListByPage(pJson);
        int total = storeService.getStoreListByPageCount(pJson);
        return SuccessMsg(list, total);
    }

    //查询详情
    @RequestMapping(value = "/getStoreByIdOld", produces = "text/plain;charset=UTF-8")
    public String getStoreByIdOld(@RequestBody JSONObject pJson){
        Map<String, Object> mainData = storeService.getStoreById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        String headerId = String.valueOf(pJson.get("bill_id"));
        List<Map<String,Object>> lines =  invBillLineService.getBillLinesByHeaderId(headerId);
        Map<String,Object> result = new HashMap<>();
        result.put("mainData",mainData);
        result.put("linesData",lines);
        return SuccessMsg("获取成功", result);
    }


    //查询详情
    @RequestMapping(value = "/getStoreById", produces = "text/plain;charset=UTF-8")
    public String getStoreById(@RequestBody JSONObject pJson){
        Map<String, Object> mainData = storeService.getStoreById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        String headerId = String.valueOf(pJson.get("bill_id"));
        List<Map<String,Object>> lines =  invBillLineService.getBillLinesByHeaderId(headerId);
        Map<String,Object> result = new HashMap<>();
        result.put("mainData",mainData);
      //  result.put("linesData",lines);


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


    //更新
    @RequestMapping(value = "/updateStoreById", produces = "text/plain;charset=UTF-8")
    public String updateStoreById(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        int userId = SysContext.getId();
        try {
            sqlSession.getConnection().setAutoCommit(false);

            //更新主实体
            JSONObject mainData = pJson.getJSONObject("mainData");

            storeService.updateStoreById(sqlSession,mainData);


            //删除行数据
            String deleteIds  = pJson.getString("deleteData");
            if(deleteIds!=null && !deleteIds.isEmpty()){
                invBillLineService.deleteBillLines(sqlSession,deleteIds);
            }

            //新增或更新行数据
            JSONArray jsonArray = pJson.getJSONArray("linesData");
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put("line_number",i);
                jsonObject.put("create_by",userId);
                jsonObject.put("header_id", mainData.get("bill_id"));
            }
            invBillLineService.saveOrUpdateBillLinesList(sqlSession,jsonArray);

            sqlSession.getConnection().commit();
            return SuccessMsg("创建成功",mainData.get("bill_id"));
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
    @RequestMapping(value = "/createStore", produces = "text/plain; charset=utf-8")
    public String createStore(@RequestBody JSONObject pJson) throws SQLException{
        int userId = SysContext.getId();
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try {

            sqlSession.getConnection().setAutoCommit(false);

            JSONObject mainData = pJson.getJSONObject("mainData");
            mainData.put("create_by",userId);

            //保存主数据
            long billId = storeService.createStore(sqlSession,mainData);

            if(billId < 0){
                return ErrorMsg("2000","创建失败");
            }

            String billType = mainData.getString("bill_type");
            String invOrgId =  mainData.getString("inv_org_id");
            String targetInvOrgId =  mainData.getString("target_inv_org_id");

            int billStatus = mainData.getIntValue("bill_status");


            List jsonArray = pJson.getJSONArray("linesData");

            //盘点的行信息可能需要额外查询
            if("count".equals(billType)){
               String inventoryType =   mainData.getString("inventory_type");
                Map<String,Object>   inventoryItemMap = new HashMap<>();
                inventoryItemMap.put("org_id",invOrgId);

                if("all".equals(inventoryType)){ // 全库盘查
                    jsonArray = invItemOnHandService.getItemOnHandInventoryItemByOrgId(inventoryItemMap);
                }else if("single".equals(inventoryType)){
                    inventoryItemMap.put("item_category_id",mainData.getString("item_category_id"));
                    jsonArray = invItemOnHandService.getItemOnHandInventoryItemByOrgIdAndCategoryId(inventoryItemMap);
                }
            }

            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject;
                Object obj = jsonArray.get(i);
                if(obj instanceof JSONObject){
                    jsonObject = (JSONObject)jsonArray.get(i);
                }else {
                    jsonObject = new JSONObject((Map<String, Object>) jsonArray.get(i));
                }
                jsonObject.put("line_number",i);
                jsonObject.put("create_by",userId);
                jsonObject.put("header_id",billId);
                jsonObject.put("create_date", DateUtil.getCurrentTimm());

                jsonObject.put("source_id", mainData.get("source_id"));
                jsonObject.put("source_system",mainData.get("source_system"));
                jsonObject.put("source_voucher",mainData.get("source_bill"));

                String itemId = jsonObject.getString("item_id");

                //采购入库 生产入库 生产出库 维护剩余数量
                if("store_po".equals(billType) || "store_pd".equals(billType) || "deliver_pd".equals(billType)){
                    //获取订单id
                    String sourceId =  mainData.getString("source_id");
                    //接收数量
                    double rcvQuantity = jsonObject.getDouble("quantity");
                    poLinesService.updatePoLinesRcvQuantity(sqlSession,sourceId,itemId,rcvQuantity);
                }

                if(0 < billStatus){
                    //添加事物
                    jsonObject.put("bill_type",billType);
                    if(billType.startsWith("store_")){ //入库为新增
                        jsonObject.put("org_id",invOrgId);
                        invItemTransactionService.weightedMean(sqlSession,jsonObject,true);
                    }else if(billType.startsWith("deliver_")){ //出库为减少
                        jsonObject.put("org_id",invOrgId);
                        invItemTransactionService.weightedMean(sqlSession,jsonObject,false);
                    }
                }
            }
            boolean isLinesSaveSuccess = invBillLineService.insertBillLinesAll(sqlSession,jsonArray);


            if(!isLinesSaveSuccess){
                sqlSession.getConnection().commit();
                return  ErrorMsg("2000","行数据保存失败");
            }

            String billTypeName = null;
            int assignerId = -1;
            String func_url = null;
            if("store_other".equals(billType)){ //其他入库
                billTypeName = "其他入库";
                assignerId = userId;
                func_url = "/transation/store/other/edit/" + billId;
            } else if("deliver_other".equals(billType)){//出库
                billTypeName= "其他出库";
                assignerId = userId;
                func_url = "/transation/deliver/other/edit/" + billId;
            }else if("transfer".equals(billType)){
                int operator = mainData.getInteger("operator");
                billTypeName = "调拨出库";
                assignerId = operator;
                func_url = "/transation/transfer/out/edit/" + billId;
            }else if("deliver_sales".equals(billType)){//销售出库
                billTypeName= "销售出库";
                assignerId = userId;
                func_url = "/sales/sales/edit/" + billId;
            }else if("deliver_wholesales".equals(billType)){//批发出库
                billTypeName = "批发出库";
                assignerId = userId;
                func_url = "/sales/sales/edit/" + billId;
            }else if("count".equals(billType)){//盘点
                int operator = mainData.getInteger("operator");
                billTypeName = "盘点";
                assignerId = operator;
                func_url = "/transation/count/edit/" + billId;
            }

            if(billTypeName!=null){
                HashMap taskMap = new HashMap<String,Object>();
                taskMap.put("task_name","您有一条新的"+ billTypeName + "任务");
                taskMap.put("owner_id",userId);
                taskMap.put("assigner_id",assignerId);
                taskMap.put("assign_date",DateUtil.getCurrentTimm());
                taskMap.put("receive_date",null);
                taskMap.put("last_update_date",DateUtil.getCurrentTimm());
                taskMap.put("complete_date",null);
                taskMap.put("task_status",0);
                taskMap.put("task_type","store");
                taskMap.put("func_url",func_url);
                taskMap.put("func_param",null);
                taskMap.put("task_description",billTypeName);
                taskMap.put("task_level",0);
                taskMap.put("source_id",billId);

                //创建任务
                fndTaskService.saveFndTask(sqlSession,taskMap);
            }



            sqlSession.getConnection().commit();
            return SuccessMsg("创建成功",billId);
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

    @RequestMapping(value = "/deleteStoreByIds", produces = "text/plain;charset=UTF-8")
    public String deleteStoreByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //删除主实体
            storeService.deleteStoreByIds(sqlSession,deleteIds);
            //删除行数据
            invBillLineService.deleteBillLinesByHeaderIds(sqlSession,deleteIds);
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


    @RequestMapping(value = "/updateStoreStatusByIds", produces = "text/plain;charset=UTF-8")
    public String updateStoreStatusByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        String deleteIds  = pJson.getString("ids");
        int billStatus = pJson.getIntValue("bill_status");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("过账失败","请选择过账项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //批量过账
            storeService.updateStoreStatusByIds(sqlSession,deleteIds,billStatus);
            String[] billIdArr =  deleteIds.split(",");
            for(String billId : billIdArr){
                //获取主信息
                Map<String,Object> billParams = new HashMap<>();
                billParams.put("bill_id",billId);
                Map<String,Object> mainObj = storeService.getStoreById(billParams);
                String billType = String.valueOf(mainObj.get("bill_type"));
                String invOrgId = String.valueOf(mainObj.get("inv_org_id"));
                String targetInvOrgId =  String.valueOf(mainObj.get("target_inv_org_id"));

                //获取子信息
                List<Map<String,Object>> billLines = invBillLineService.getBillLinesByHeaderId(billId);

                for(Map<String,Object> billLine : billLines){
                    billLine.put("bill_type",billType);
                    billLine.put("org_id",invOrgId);
                    if(billType.startsWith("store_")){ //入库为新增
                        invItemTransactionService.weightedMean(sqlSession,billLine,true);
                    }else if(billType.startsWith("deliver_")){ //出库为减少
                        invItemTransactionService.weightedMean(sqlSession,billLine,false);
                    }else if("transfer".equals(billType)){//调拨
                        if(2 == billStatus){
                            //调拨记录两笔

                            //调出仓库减少
                            billLine.put("org_id",invOrgId);//调出仓库
                            billLine.put("bill_type","transfer_out");
                            invItemTransactionService.weightedMean(sqlSession,billLine,false);

                            //调入仓库增加
                            billLine.put("org_id",targetInvOrgId);//调入仓库
                            billLine.put("bill_type","transfer_in");
                            invItemTransactionService.weightedMean(sqlSession,billLine,true);

                        }
                    }
                }



                //更新任务
                HashMap taskMap = new HashMap<String,Object>();
                taskMap.put("last_update_date",DateUtil.getCurrentTimm());
                taskMap.put("complete_date",DateUtil.getCurrentTimm());
                taskMap.put("task_status",2);
                taskMap.put("task_type","store");
                taskMap.put("source_id",billId);
                fndTaskService.updateFndTaskBySourceIdAndTaskType(sqlSession,taskMap);

                if("transfer".equals(billType) && 1 == billStatus){
                    HashMap createTaskMap = new HashMap<String,Object>();
                    createTaskMap.put("task_name","您有一条新的调拨入库任务");
                    createTaskMap.put("owner_id",mainObj.get("create_by"));
                    createTaskMap.put("assigner_id",mainObj.get("target_operator"));
                    createTaskMap.put("assign_date",DateUtil.getCurrentTimm());
                    createTaskMap.put("receive_date",null);
                    createTaskMap.put("last_update_date",DateUtil.getCurrentTimm());
                    createTaskMap.put("complete_date",null);
                    createTaskMap.put("task_status",0);
                    createTaskMap.put("task_type","store");
                    createTaskMap.put("func_url","/transation/transfer/in/edit/" + billId);
                    createTaskMap.put("func_param",null);
                    createTaskMap.put("task_description","调拨入库");
                    createTaskMap.put("task_level",0);
                    createTaskMap.put("source_id",billId);
                    //创建任务
                    fndTaskService.saveFndTask(sqlSession,createTaskMap);
                }
            }

            sqlSession.getConnection().commit();
            return SuccessMsg("过账成功","");
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }







}
