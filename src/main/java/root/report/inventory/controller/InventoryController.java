package root.report.inventory.controller;

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
import root.report.inventory.service.InventoryService;
import root.report.itemCategory.service.ItemCategoryService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportServer/inventory")
public class InventoryController extends RO {

    private static Logger log = Logger.getLogger(InventoryController.class);

    @Autowired
    public InventoryService inventoryService;

    @RequestMapping(value = "/getAllPage", produces = "text/plain;charset=UTF-8")
    public String getAllPage(@RequestBody String pJson) {
        try {
            JSONObject jsonFunc = JSONObject.parseObject(pJson);
            Map<String,String> map=new HashMap();
            map.put("startIndex",jsonFunc.getString("startIndex"));
            map.put("perPage",jsonFunc.getString("perPage"));
            map.put("item_description",jsonFunc.getString("item_description"));
            map.put("item_category_id",jsonFunc.getString("item_category_id"));
            map.put("item_id",jsonFunc.getString("item_id"));
            map.put("org_id",jsonFunc.getString("org_id"));
            Map<String,Object> map1 = inventoryService.getAllPage(map);
            return SuccessMsg("", map1);
        } catch (Exception ex){
            return ExceptionMsg(ex.getMessage());
        }
    }


}
