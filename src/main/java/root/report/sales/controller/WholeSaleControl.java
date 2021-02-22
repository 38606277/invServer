package root.report.sales.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.inv.store.InvItemTransactionService;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.itemCategory.service.ItemCategoryService;
import root.report.sales.service.WholeSaleLineService;
import root.report.sales.service.WholeSaleService;
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
@RequestMapping(value = "/reportServer/wholeSale")
public class WholeSaleControl extends RO {

    @Autowired
    public WholeSaleService wholeSaleService;

    @Autowired
    WholeSaleLineService wholeSaleLineService;

    @Autowired
    InvItemTransactionService invItemTransactionService;

    @Autowired
    public ItemCategoryService itemCategoryService;


    //查询所有事物
    @RequestMapping(value = "/getWholeSaleListByPage", produces = "text/plain;charset=UTF-8")
    public String getWholeSaleListByPage(@RequestBody JSONObject pJson) {
        try{
            Map<String,Object> map1 = wholeSaleService.getWholeSaleListByPage(pJson);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    //查询详情
    @RequestMapping(value = "/getWholeSaleById", produces = "text/plain;charset=UTF-8")
    public String getWholeSaleById(@RequestBody JSONObject pJson){
        Map<String, Object> mainData = wholeSaleService.getWholeSaleById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        String headerId = String.valueOf(pJson.get("so_header_id"));
        List<Map<String,Object>> lines =  wholeSaleLineService.getBillLinesByHeaderId(headerId);
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
    @RequestMapping(value = "/updateWholeSaleById", produces = "text/plain;charset=UTF-8")
    public String updateWholeSaleById(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        int userId = SysContext.getId();
        try {
            sqlSession.getConnection().setAutoCommit(false);

            //更新主实体
            JSONObject mainData = pJson.getJSONObject("mainData");
            String orgid=mainData.getString("inv_org_id");
            wholeSaleService.updateWholeSaleById(sqlSession,mainData);


            //删除行数据
            String deleteIds  = pJson.getString("deleteData");
            if(deleteIds!=null && !deleteIds.isEmpty()){
                wholeSaleLineService.deleteBillLines(sqlSession,deleteIds);
            }

            //新增或更新行数据
            JSONArray jsonArray = pJson.getJSONArray("linesData");
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put("line_number",i);
                jsonObject.put("create_by",userId);
                jsonObject.put("header_id", mainData.get("so_header_id"));
            }
            wholeSaleLineService.saveOrUpdateBillLinesList(sqlSession,jsonArray,orgid);

            sqlSession.getConnection().commit();
            return SuccessMsg("创建成功",mainData.get("so_header_id"));
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
    @RequestMapping(value = "/createWholeSale", produces = "text/plain; charset=utf-8")
    public String createWholeSale(@RequestBody JSONObject pJson) throws SQLException{
        int userId = SysContext.getId();
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject mainData = pJson.getJSONObject("mainData");
            mainData.put("create_by",userId);
            //保存主数据
            long billId = wholeSaleService.createWholeSale(sqlSession,mainData);
            if(billId < 0){
                return ErrorMsg("2000","创建失败");
            }
            String billType = mainData.getString("so_type");
            String invOrgId =  mainData.getString("inv_org_id");
            int status = mainData.getIntValue("status");
            JSONArray jsonArray = pJson.getJSONArray("linesData");

            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put("line_number",i);
                jsonObject.put("create_by",userId);
                jsonObject.put("header_id",billId);
                jsonObject.put("create_date", DateUtil.getCurrentTimm());

                jsonObject.put("category_id", mainData.get("source_id"));
                jsonObject.put("line_type_id",1);

                if(0 < status){
                    //添加事物
                    jsonObject.put("so_type",billType);
                     //出库为减少
                    jsonObject.put("org_id",invOrgId);
                    invItemTransactionService.weightedMean(sqlSession,jsonObject,false);
                }
            }
            boolean isLinesSaveSuccess = wholeSaleLineService.insertBillLinesAll(sqlSession,jsonArray);

            if(!isLinesSaveSuccess){
                return  ErrorMsg("2000","行数据保存失败");
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

    @RequestMapping(value = "/deleteWholeSaleByIds", produces = "text/plain;charset=UTF-8")
    public String deleteWholeSaleByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //更新主实体
            wholeSaleService.deleteWholeSaleByIds(sqlSession,deleteIds);
            //删除行数据
            wholeSaleLineService.deleteBillLines(sqlSession,deleteIds);
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


    @RequestMapping(value = "/updateWholeSaleStatusByIds", produces = "text/plain;charset=UTF-8")
    public String updateWholeSaleStatusByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        String deleteIds  = pJson.getString("ids");
        int billStatus = pJson.getIntValue("status");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("过账失败","请选择过账项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //批量过账
            wholeSaleService.updateWholeSaleStatusByIds(sqlSession,deleteIds,billStatus);
            String[] billIdArr =  deleteIds.split(",");
            for(String billId : billIdArr){
                //获取主信息
                Map<String,Object> billParams = new HashMap<>();
                billParams.put("so_header_id",billId);
                Map<String,Object> mainObj = wholeSaleService.getWholeSaleById(billParams);
                String billType = String.valueOf(mainObj.get("so_type"));
                String invOrgId = String.valueOf(mainObj.get("inv_org_id"));

                //获取子信息
                List<Map<String,Object>> billLines = wholeSaleLineService.getBillLinesByHeaderId(billId);

                for(Map<String,Object> billLine : billLines){
                    billLine.put("so_type",billType);
                    billLine.put("org_id",invOrgId);
                    invItemTransactionService.weightedMeanForSales(sqlSession,billLine,false);
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
