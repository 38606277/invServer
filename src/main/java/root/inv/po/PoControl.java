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
    PoHeadersService poHeadersService;

    @Autowired
    PoLinesService poLinesService;

    @Autowired
    public ItemCategoryService itemCategoryService;

    //查询所有订单
    @RequestMapping(value = "/getPoListByPage", produces = "text/plain;charset=UTF-8")
    public String getPoListByPage(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }

        pJson.put("startIndex",currentPage);
        pJson.put("perPage",perPage);

        List<Map<String, Object>> list = poHeadersService.getPoHeadersListByPage(pJson);
        int total = poHeadersService.getPoHeadersListByPageCount(pJson);
        return SuccessMsg(list, total);
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
            return ErrorMsg("过账失败","请选择过账项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //批量过账
            poHeadersService.updatePoHeadersStatusByIds(sqlSession,deleteIds,status);
//            String[] billIdArr =  deleteIds.split(",");
//            for(String billId : billIdArr){
//                //获取主信息
//                Map<String,Object> billParams = new HashMap<>();
//                billParams.put("bill_id",billId);
//                Map<String,Object> mainObj = storeService.getPoById(billParams);
//                String billType =  String.valueOf(mainObj.get("bill_type"));
//                String invOrgId =  String.valueOf(mainObj.get("inv_org_id"));
//
//                //获取子信息
//                List<Map<String,Object>> billLines = invBillLineService.getBillLinesById(billParams);
//
//                for(Map<String,Object> billLine : billLines){
//                    billLine.put("bill_type",billType);
//                    billLine.put("org_id",invOrgId);
//
//                    if("store".equals(billType)){ //入库为新增
//                        invItemTransactionService.weightedMean(sqlSession,billLine,true);
//                    }else if("deliver".equals(billType)){ //出库为减少
//                        invItemTransactionService.weightedMean(sqlSession,billLine,false);
//                    }
//
//                }
//            }
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
