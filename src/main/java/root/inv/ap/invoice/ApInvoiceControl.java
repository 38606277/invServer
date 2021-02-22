package root.inv.ap.invoice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
 * 发票
 */
@RestController
@RequestMapping(value = "/reportServer/ap/invoice")
public class ApInvoiceControl extends RO {

    @Autowired
    ApInvoiceService apInvoiceService;

    @Autowired
    ApInvoiceLinesService apInvoiceLinesService;

    @Autowired
    public ItemCategoryService itemCategoryService;

    //查询所有订单
    @RequestMapping(value = "/getInvoiceListByPage", produces = "text/plain;charset=UTF-8")
    public String getInvoiceListByPage(@RequestBody JSONObject pJson) {
        Map<String,Object> result = apInvoiceService.getApInvoiceListByPage(pJson);
        return SuccessMsg("", result);
    }

    //查询详情
    @RequestMapping(value = "/getInvoiceById", produces = "text/plain;charset=UTF-8")
    public String getInvoiceById(@RequestBody JSONObject pJson){

        Map<String, Object> mainData = apInvoiceService.getApInvoiceById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        String headId  = String.valueOf(mainData.get("invoice_id"));
        List<Map<String,Object>> lines =  apInvoiceLinesService.getApInvoiceLinesByInvoiceId(headId);
        Map<String,Object> result = new HashMap<>();
        result.put("mainData",mainData);
//
//        Map<String,List<Map>> columnMap = new HashMap<>();
//        Map<String,List<Map>> lineDateMap = new HashMap<>();
//        Map<String,String> categoryNameMap = new HashMap<>();
//        for(Map<String,Object> map : lines){
//            String itemCategoryId = String.valueOf(map.get("item_category_id"));
//            String itemCategoryName = String.valueOf(map.get("category_name"));
//            if(!columnMap.containsKey(itemCategoryId)){
//                Map<String,Object> params = new HashMap<>();
//                params.put("category_id",itemCategoryId);
//
//                //动态列
//                List<Map> columnList = itemCategoryService.getItemCategorySegmentByPid(params);
//                for(Map columnItemMap :columnList ){
//                    columnItemMap.put("title",columnItemMap.get("segment_name"));
//                    columnItemMap.put("dataIndex",columnItemMap.get("segment"));
//                }
//                columnMap.put(itemCategoryId,columnList);
//                categoryNameMap.put(itemCategoryId,itemCategoryName);
//                //行数据
//                List<Map> lineList = new ArrayList<>();
//                lineList.add(map);
//                lineDateMap.put(itemCategoryId,lineList);
//            }else{
//                List<Map> lineList =  lineDateMap.get(itemCategoryId);
//                lineList.add(map);
//            }
//        }
//
//        JSONArray jsonArray = new JSONArray();
//        for(String key : columnMap.keySet()){
//            List<Map> column  = columnMap.get(key);
//            List<Map> dataList = lineDateMap.get(key);
//            String categoryName =  categoryNameMap.get(key);
//            JSONObject jsonObject = new JSONObject();
//
//            jsonObject.put("columnList",column);
//            jsonObject.put("dataList",dataList);
//            jsonObject.put("categoryName",categoryName);
//            jsonObject.put("categoryId",key);
//            jsonArray.add(jsonObject);
//        }

        result.put("linesData",lines);
        return SuccessMsg("获取成功", result);
    }

    @RequestMapping(value = "/getApInvoiceLinesById", produces = "text/plain;charset=UTF-8")
    public String getInvoiceListById(@RequestBody JSONObject pJson){
        String headId = String.valueOf(pJson.get("invoice_id"));
        List<Map<String,Object>> lines =  apInvoiceLinesService.getApInvoiceLinesByInvoiceId(headId);
        return SuccessMsg(lines, lines.size());
    }

    @RequestMapping(value = "/getApInvoiceLinesColumnById", produces = "text/plain;charset=UTF-8")
    public String getApInvoiceLinesColumnById(@RequestBody JSONObject pJson){
        String headId = String.valueOf(pJson.get("invoice_id"));
        List<Map<String,Object>> lines =  apInvoiceLinesService.getApInvoiceLinesByInvoiceId(headId);
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
    @RequestMapping(value = "/updateInvoiceById", produces = "text/plain;charset=UTF-8")
    public String updateInvoiceById(@RequestBody JSONObject pJson)throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        int userId = SysContext.getId();
        try {
            sqlSession.getConnection().setAutoCommit(false);

            //更新主实体
            JSONObject mainData = pJson.getJSONObject("mainData");

            apInvoiceService.updateApInvoiceById(sqlSession,mainData);


            //删除行数据
            String deleteIds  = pJson.getString("deleteData");
            if(deleteIds!=null && !deleteIds.isEmpty()){
                apInvoiceLinesService.deleteApInvoiceLines(sqlSession,deleteIds);
            }

            //新增或更新行数据
            JSONArray jsonArray = pJson.getJSONArray("linesData");
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put("line_number",i);
                jsonObject.put("create_by",userId);
                jsonObject.put("header_id", mainData.get("invoice_id"));
            }
            apInvoiceLinesService.saveOrUpdateApInvoiceLinesList(sqlSession,jsonArray);

            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",mainData.get("invoice_id"));
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
    @RequestMapping(value = "/createInvoice", produces = "text/plain; charset=utf-8")
    public String createInvoice(@RequestBody JSONObject pJson) throws SQLException{
        int userId = SysContext.getId();
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try {

            sqlSession.getConnection().setAutoCommit(false);

            JSONObject mainData = pJson.getJSONObject("mainData");
            mainData.put("create_by",userId);
            mainData.put("update_by",userId);
            mainData.put("create_date", DateUtil.getCurrentTimm());
            mainData.put("update_date", DateUtil.getCurrentTimm());


//            String billCode = sqlSession.selectOne("fnd_order_number_setting.getOrderNumber","po_order");
//            mainData.put("header_code", billCode);

            //保存主数据
            long id = apInvoiceService.saveApInvoice(sqlSession,mainData);

            if(id < 0){
                return ErrorMsg("2000","创建失败");
            }

            JSONArray jsonArray = pJson.getJSONArray("linesData");
            if(jsonArray !=null && 0 <jsonArray.size()){
                for(int i = 0; i < jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    jsonObject.put("invoice_id",id);
                }
                apInvoiceLinesService.insertApInvoiceLinesAll(sqlSession,jsonArray);
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

    @RequestMapping(value = "/deleteInvoiceByIds", produces = "text/plain;charset=UTF-8")
    public String deleteInvoiceByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //更新主实体
            apInvoiceService.deleteApInvoiceByIds(sqlSession,deleteIds);
            //删除行数据
            apInvoiceLinesService.deleteApInvoiceLinesByInvoiceIds(sqlSession,deleteIds);
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


}
