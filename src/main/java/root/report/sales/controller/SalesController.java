package root.report.sales.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import root.report.sys.SysContext;
import root.report.util.DateUtil;

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

    @RequestMapping(value = "/getAllPage", produces = "text/plain;charset=UTF-8")
    public String getAllPage(@RequestBody JSONObject pJson) {
        try {
            Map<String,String> map=new HashMap();
            map.put("startIndex",pJson.getString("startIndex"));
            map.put("perPage",pJson.getString("perPage"));
            map.put("item_description",pJson.getString("item_description"));
            String category_idlist=pJson.getString("category_id")==null?"":pJson.getString("category_id");
            String categoryidInID = "";
            if(null!=category_idlist && !"".equals(category_idlist)) {
                String[] arr = category_idlist.split(",");
                for (String id : arr) {
                    categoryidInID = categoryidInID + ",'" + id +"'";
                }
                if (null != categoryidInID && !"".equals(categoryidInID)) {
                    categoryidInID = categoryidInID.substring(1, categoryidInID.length());
                }
            }
            map.put("item_category_id",categoryidInID);
            String org_idlist=pJson.getString("org_id")==null?"":pJson.getString("org_id");
            String orgInid = "";
            if(null!=org_idlist && !"".equals(org_idlist)) {
                String[] arr = org_idlist.split(",");
                for (String id : arr) {
                    orgInid = orgInid + ",'" + id +"'";
                }
                if (null != orgInid && !"".equals(orgInid)) {
                    orgInid = orgInid.substring(1, orgInid.length());
                }
            }
            map.put("org_id",orgInid);
            Map<String,Object> map1 = salesService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
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

    /********************添加商品*****************/

    @RequestMapping(value = "/addCategory", produces = "text/plain;charset=UTF-8")
    public String addCategory(@RequestBody JSONObject pJson) {
        try{
            Map map = new HashMap();
            String sales_id=pJson.getString("sales_id");
            Map m = salesService.getSalesOrderByID(sales_id,"0",pJson);

            return SuccessMsg("查询成功",map);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }
    @RequestMapping(value = "/getCarSales", produces = "text/plain;charset=UTF-8")
    public String getCarSales() {
        try{
            Map map = new HashMap();
            int userId = SysContext.getId();
            map= salesService.getCarSalesByID(userId+"","0",null);
            return SuccessMsg("查询成功",map);
        }catch (Exception ex){
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

}
