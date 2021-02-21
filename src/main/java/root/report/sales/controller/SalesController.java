package root.report.sales.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.itemCategory.service.ItemCategoryService;
import root.report.sales.service.SalesService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/sales")
public class SalesController extends RO {

    private static Logger log = Logger.getLogger(SalesController.class);

    @Autowired
    public SalesService salesService;

    @Autowired
    public ItemCategoryService itemCategoryService;

    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     */
    @RequestMapping(value = "/saveShipment", produces = "text/plain;charset=UTF-8")
    public String saveDict(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            Map resmap = this.salesService.saveOrUpdateShipment(sqlSession, jsonObject);
            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",resmap);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getAllPage", produces = "text/plain;charset=UTF-8")
    public String getAllPage(@RequestBody String pJson) {
        try {

            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("startIndex",jsonFunc.getString("startIndex"));
            map.put("perPage",jsonFunc.getString("perPage"));

            Map<String,Object> map1 = salesService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    //返回数据
    @RequestMapping(value = "/getShipmentByID", produces = "text/plain;charset=UTF-8")
    public String getShipmentByID(@RequestBody String pjson) {
        JSONObject obj=JSON.parseObject(pjson);
        try{
            Map map = new HashMap();
            map.put("vendor_id",obj.get("vendor_id"));
            Map jsonObject = this.salesService.getShipmentByID(map);
            return SuccessMsg("查询成功",jsonObject);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }




    // 从json数据当中解析 ，批量删除
    @RequestMapping(value = "/deleteShipmentById", produces = "text/plain;charset=UTF-8")
    public String deleteShipmentById(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject obj=JSON.parseObject(pJson);
            String ids = obj.getString("vendor_id");
            String[] arrId=ids.split(",");
            for(int i = 0; i < arrId.length; i++){
                //删除
                this.salesService.deleteShipmentById(sqlSession,arrId[0]);
            }
            sqlSession.getConnection().commit();
            return SuccessMsg("删除成功",null);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

    //获取分类和仓库数据
    @RequestMapping(value = "/getItemCategoryAndOrg", produces = "text/plain;charset=UTF-8")
    public String getItemCategoryAndOrg() {
        try{
            Map map = new HashMap();
            List<Map> itemcateList = itemCategoryService.getAll();
            List<Map> orgList = this.salesService.getOrgAll();
            map.put("itemcateList",itemcateList);
            map.put("orgList",orgList);
            return SuccessMsg("查询成功",map);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

}
