package root.inv.pd;

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
import root.report.util.UUIDUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生产订单
 */
@RestController
@RequestMapping(value = "/reportServer/pd")
public class PdControl extends RO {

    @Autowired
    PdOrderHeaderService pdOrderHeaderService;

    @Autowired
    PdOrderLinesService pdOrderLinesService;

    @Autowired
    public ItemCategoryService itemCategoryService;

    @RequestMapping(value = "/getListByPage", produces = "text/plain;charset=UTF-8")
    public String getPdListByPage(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }

        pJson.put("startIndex",currentPage);
        pJson.put("perPage",perPage);

        List<Map<String, Object>> list = pdOrderHeaderService.getPdOrderHeaderListByPage(pJson);
        int total = pdOrderHeaderService.getPdOrderHeaderListByPageCount(pJson);
        return SuccessMsg(list, total);
    }

    //查询详情
    @RequestMapping(value = "/getPdById", produces = "text/plain;charset=UTF-8")
    public String getPdById(@RequestBody JSONObject pJson){
        Map<String, Object> mainData = pdOrderHeaderService.getPdOrderHeaderById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        String itemId  = String.valueOf(mainData.get("pd_header_id"));

        List<Map<String,Object>> lines = pdOrderLinesService.getPdOrderLinesByItemId(itemId);
        Map<String,Object> result = new HashMap<>();
        result.put("mainData",mainData);
        result.put("linesData",lines);
        return SuccessMsg("获取成功", result);
    }


    @RequestMapping(value = "/getPdOrderLinesById", produces = "text/plain;charset=UTF-8")
    public String getPdListById(@RequestBody JSONObject pJson){
        List<Map<String,Object>> lines = pdOrderLinesService.getPdOrderLinesByItemId(pJson);
        return SuccessMsg(lines, lines.size());
    }

    @RequestMapping(value = "/getPdLinesColumnById", produces = "text/plain;charset=UTF-8")
    public String getPoLinesColumnById(@RequestBody JSONObject pJson){
        String headId = String.valueOf(pJson.get("pd_header_id"));
        List<Map<String,Object>> lines =  pdOrderLinesService.getPdOrderLinesByItemId(headId);
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
    @RequestMapping(value = "/updatePdById", produces = "text/plain;charset=UTF-8")
    public String updatePdById(@RequestBody JSONObject pJson)throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        int userId = SysContext.getId();
        try {
            sqlSession.getConnection().setAutoCommit(false);

            //更新主实体
            JSONObject mainData = pJson.getJSONObject("mainData");
            pdOrderHeaderService.updatePdOrderHeaderById(sqlSession,mainData);

            //删除行数据
            String deleteIds  = pJson.getString("deleteData");
            if(deleteIds!=null && !deleteIds.isEmpty()){
                pdOrderLinesService.deletePdOrderLines(sqlSession,deleteIds);
            }

            //新增或更新行数据

            JSONArray jsonArray = pJson.getJSONArray("linesData");
            pdOrderLinesService.insetOrUpdate(sqlSession,mainData.getString("pd_header_id"),jsonArray);
            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",mainData.get("pd_header_id"));
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


    @RequestMapping(value = "/createPd", produces = "text/plain; charset=utf-8")
    public String createPd(@RequestBody JSONObject pJson) throws SQLException{
        int userId = SysContext.getId();
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try {

            sqlSession.getConnection().setAutoCommit(false);

            JSONObject mainData = pJson.getJSONObject("mainData");
            mainData.put("create_by",userId);
            mainData.put("update_by",userId);
            mainData.put("create_date", DateUtil.getCurrentTimm());
            mainData.put("update_date", DateUtil.getCurrentTimm());

            String billCode = sqlSession.selectOne("fnd_order_number_setting.getOrderNumber","pd_order");
            mainData.put("pd_header_code", billCode);

            //保存主数据
            long id = pdOrderHeaderService.savePdOrderHeader(sqlSession,mainData);

            if(id < 0){
                return ErrorMsg("2000","创建失败");
            }

            JSONArray jsonArray = pJson.getJSONArray("linesData");
            pdOrderLinesService.insetOrUpdate(sqlSession,String.valueOf(id),jsonArray);

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

    @RequestMapping(value = "/deleteByIds", produces = "text/plain;charset=UTF-8")
    public String deletePdByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //更新主实体
            pdOrderHeaderService.deletePdOrderHeaderByIds(sqlSession,deleteIds);
            //删除行数据
            pdOrderLinesService.deletePdLinesByHeaderIds(sqlSession,deleteIds);
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
