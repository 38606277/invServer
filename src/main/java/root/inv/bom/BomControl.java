package root.inv.bom;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.sys.SysContext;
import root.report.util.DateUtil;
import root.report.util.UUIDUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品
 */
@RestController
@RequestMapping(value = "/reportServer/bom")
public class BomControl extends RO {

    @Autowired
    BomHeadersService bomHeadersService;

    @Autowired
    BomLinesService bomLinesService;

    @RequestMapping(value = "/getListByPage", produces = "text/plain;charset=UTF-8")
    public String getBomListByPage(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }

        pJson.put("startIndex",currentPage);
        pJson.put("perPage",perPage);

        List<Map<String, Object>> list = BomHeadersService.getBomHeadersListByPage(pJson);
        int total = bomHeadersService.getBomHeadersListByPageCount(pJson);
        return SuccessMsg(list, total);
    }

    //查询详情
    @RequestMapping(value = "/getBomById", produces = "text/plain;charset=UTF-8")
    public String getBomById(@RequestBody JSONObject pJson){
        Map<String, Object> mainData = bomHeadersService.getBomHeadersById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        String itemId  = String.valueOf(mainData.get("item_id"));
        List<Map<String,Object>> lines = bomLinesService.getAllChildrenRecursionByItemId(itemId);
        Map<String,Object> result = new HashMap<>();
        result.put("mainData",mainData);
        result.put("linesData",lines);
        return SuccessMsg("获取成功", result);
    }


    @RequestMapping(value = "/getBomLinesById", produces = "text/plain;charset=UTF-8")
    public String getBomListById(@RequestBody JSONObject pJson){
        String itemId = String.valueOf(pJson.get("item_id"));
        List<Map<String,Object>> lines = bomLinesService.getAllChildrenRecursionByItemId(itemId);
        return SuccessMsg(lines, lines.size());
    }



    //更新
    @RequestMapping(value = "/updateBomById", produces = "text/plain;charset=UTF-8")
    public String updateBomById(@RequestBody JSONObject pJson)throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        int userId = SysContext.getId();
        try {
            sqlSession.getConnection().setAutoCommit(false);

            //更新主实体
            JSONObject mainData = pJson.getJSONObject("mainData");
            bomHeadersService.updateBomHeadersById(sqlSession,mainData);

            //删除行数据
            String deleteIds  = pJson.getString("deleteData");
            if(deleteIds!=null && !deleteIds.isEmpty()){
                bomLinesService.deleteBomLines(sqlSession,deleteIds);
            }

            //新增或更新行数据

            JSONArray jsonArray = pJson.getJSONArray("linesData");
            bomLinesService.insetOrUpdate(sqlSession,mainData.getString("item_id"),jsonArray);
            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",mainData.get("item_id"));
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }


    @RequestMapping(value = "/createBom", produces = "text/plain; charset=utf-8")
    public String createBom(@RequestBody JSONObject pJson) throws SQLException{
        int userId = SysContext.getId();
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try {

            sqlSession.getConnection().setAutoCommit(false);

            JSONObject mainData = pJson.getJSONObject("mainData");
            mainData.put("create_by",userId);
            mainData.put("update_by",userId);
            mainData.put("create_date", DateUtil.getCurrentTimm());
            mainData.put("update_date", DateUtil.getCurrentTimm());

            //保存主数据
            long id = bomHeadersService.saveBomHeaders(sqlSession,mainData);

            if(id < 0){
                return ErrorMsg("2000","创建失败");
            }

            JSONArray jsonArray = pJson.getJSONArray("linesData");
            bomLinesService.insetOrUpdate(sqlSession,String.valueOf(id),jsonArray);

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
    public String deleteBomByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //更新主实体
            bomHeadersService.deleteBomHeadersByIds(sqlSession,deleteIds);
            //删除行数据
            bomLinesService.deleteBomLinesByHeaderIds(sqlSession,deleteIds);
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
