package root.report.itemCategory.controller;

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
import root.report.itemCategory.service.ItemService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/item")
public class ItemController extends RO {

    private static Logger log = Logger.getLogger(ItemController.class);

    @Autowired
    public ItemService itemService;

    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     */
    @RequestMapping(value = "/saveItem", produces = "text/plain;charset=UTF-8")
    public String saveDict(@RequestBody String pJson) throws Exception
    {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String id = this.itemService.saveOrUpdate(sqlSession, jsonObject);

            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",id);
        }catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    /**
     * 根据类别ID获取数据
     * */
    @RequestMapping(value = "/getAllPage", produces = "text/plain;charset=UTF-8")
    public String getAllPage(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("startIndex",jsonFunc.getString("startIndex"));
            map.put("perPage",jsonFunc.getString("perPage"));
            map.put("item_category_id",jsonFunc.getString("item_category_id"));
            map.put("item_description",jsonFunc.getString("item_description"));
            Map<String,Object> map1 = itemService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getItemByItemId", produces = "text/plain;charset=UTF-8")
    public String getItemByItemId(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("item_id",jsonFunc.getString("item_id"));
            Map<String,Object> map1 = itemService.getItemByItemId(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    @RequestMapping(value = "/getAllPageByCategoryId", produces = "text/plain;charset=UTF-8")
    public String getAllPageByCategoryId(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("item_category_id",jsonFunc.getString("item_category_id"));
            Map<String,Object> map1 = itemService.getAllPageByCategoryId(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/deleteItemById", produces = "text/plain;charset=UTF-8")
    public String deleteItemById(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try{
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject obj=JSON.parseObject(pJson);
            String ids = obj.getString("item_id");
            String[] arrId=ids.split(",");
            for(int i = 0; i < arrId.length; i++){
                //删除
                this.itemService.deleteItemByID(sqlSession, arrId[0]);
            }
            sqlSession.getConnection().commit();
            return SuccessMsg("删除成功",null);
        }catch (Exception ex){

            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

}
