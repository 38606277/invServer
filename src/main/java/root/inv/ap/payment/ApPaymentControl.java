package root.inv.ap.payment;

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
 * 付款
 */
@RestController
@RequestMapping(value = "/reportServer/ap/payment")
public class ApPaymentControl extends RO {

    @Autowired
    ApPaymentService apPaymentService;

    @Autowired
    ApPaymentLinesService apPaymentLinesService;

    @Autowired
    public ItemCategoryService itemCategoryService;

    //查询所有订单
    @RequestMapping(value = "/getPaymentListByPage", produces = "text/plain;charset=UTF-8")
    public String getPaymentListByPage(@RequestBody JSONObject pJson) {
        Map<String,Object> result = apPaymentService.getApPaymentListByPage(pJson);
        return SuccessMsg("", result);
    }

    //查询详情
    @RequestMapping(value = "/getPaymentById", produces = "text/plain;charset=UTF-8")
    public String getPaymentById(@RequestBody JSONObject pJson){

        Map<String, Object> mainData = apPaymentService.getApPaymentById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        String headId  = String.valueOf(mainData.get("payment_id"));
        List<Map<String,Object>> lines =  apPaymentLinesService.getApPaymentLinesByPaymentId(headId);
        Map<String,Object> result = new HashMap<>();
        result.put("mainData",mainData);
        result.put("linesData",lines);
        return SuccessMsg("获取成功", result);
    }

    @RequestMapping(value = "/getApPaymentLinesById", produces = "text/plain;charset=UTF-8")
    public String getPaymentListById(@RequestBody JSONObject pJson){
        String headId = String.valueOf(pJson.get("payment_id"));
        List<Map<String,Object>> lines =  apPaymentLinesService.getApPaymentLinesByPaymentId(headId);
        return SuccessMsg(lines, lines.size());
    }

    @RequestMapping(value = "/getApPaymentLinesColumnById", produces = "text/plain;charset=UTF-8")
    public String getApPaymentLinesColumnById(@RequestBody JSONObject pJson){
        String headId = String.valueOf(pJson.get("payment_id"));
        List<Map<String,Object>> lines =  apPaymentLinesService.getApPaymentLinesByPaymentId(headId);
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
    @RequestMapping(value = "/updatePaymentById", produces = "text/plain;charset=UTF-8")
    public String updatePaymentById(@RequestBody JSONObject pJson)throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        int userId = SysContext.getId();
        try {
            sqlSession.getConnection().setAutoCommit(false);

            //更新主实体
            JSONObject mainData = pJson.getJSONObject("mainData");

            apPaymentService.updateApPaymentById(sqlSession,mainData);


            //删除行数据
            String deleteIds  = pJson.getString("deleteData");
            if(deleteIds!=null && !deleteIds.isEmpty()){
                apPaymentLinesService.deleteApPaymentLines(sqlSession,deleteIds);
            }

            //新增或更新行数据
            JSONArray jsonArray = pJson.getJSONArray("linesData");
            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put("payment_id", mainData.get("payment_id"));
            }
            apPaymentLinesService.saveOrUpdateApPaymentLinesList(sqlSession,jsonArray);

            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",mainData.get("payment_id"));
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
    @RequestMapping(value = "/createPayment", produces = "text/plain; charset=utf-8")
    public String createPayment(@RequestBody JSONObject pJson) throws SQLException{
        int userId = SysContext.getId();
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try {

            sqlSession.getConnection().setAutoCommit(false);

            JSONObject mainData = pJson.getJSONObject("mainData");
            mainData.put("create_by",userId);
            mainData.put("update_by",userId);
            mainData.put("create_date", DateUtil.getCurrentTimm());
            mainData.put("update_date", DateUtil.getCurrentTimm());

            String billCode = sqlSession.selectOne("fnd_order_number_setting.getOrderNumber","payment");
            mainData.put("payment_number", billCode);

            //保存主数据
            long id = apPaymentService.saveApPayment(sqlSession,mainData);

            if(id < 0){
                return ErrorMsg("2000","创建失败");
            }

            JSONArray jsonArray = pJson.getJSONArray("linesData");
            if(jsonArray !=null && 0 <jsonArray.size()){
                for(int i = 0; i < jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    jsonObject.put("payment_id",id);
                }
                apPaymentLinesService.insertApPaymentLinesAll(sqlSession,jsonArray);
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

    @RequestMapping(value = "/deletePaymentByIds", produces = "text/plain;charset=UTF-8")
    public String deletePaymentByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //更新主实体
            apPaymentService.deleteApPaymentByIds(sqlSession,deleteIds);
            //删除行数据
            apPaymentLinesService.deleteApPaymentLinesByPaymentIds(sqlSession,deleteIds);
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
