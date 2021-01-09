package root.inv.store;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.service.DictService;
import root.report.sys.SysContext;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入库
 */
@RestController
@RequestMapping(value = "/reportServer/invStore")
public class StoreControl extends RO {


    @Autowired
    public StoreService storeService;

    @Autowired
    InvBillLineService invBillLineService;

    //查询所有事物
    @RequestMapping(value = "/getStoreListByPage", produces = "text/plain;charset=UTF-8")
    public String getStoreListByPage(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("startIndex", currentPage);
        map.put("perPage", perPage);
        List<Map<String, Object>> list = storeService.getStoreListByPage(map);
        int total = storeService.getStoreListByPageCount(map);
        return SuccessMsg(list, total);
    }

    //查询详情
    @RequestMapping(value = "/getStoreById", produces = "text/plain;charset=UTF-8")
    public String getStoreById(@RequestBody JSONObject pJson){
        Map<String, Object> mainData = storeService.getStoreById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        List<Map<String,Object>> lines =  invBillLineService.getBillLinesById(pJson);
        Map<String,Object> result = new HashMap<>();
        result.put("mainData",mainData);
        result.put("linesData",lines);
        return SuccessMsg("获取成功", result);
    }


    /**
     * 新增事物
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/createStore", produces = "text/plain; charset=utf-8")
    public String createStore(@RequestBody JSONObject pJson) {
        String userCode = SysContext.getUserCode();

        JSONObject mainData = pJson.getJSONObject("mainData");
        mainData.put("create_by",userCode);

        //保存主数据
        long billId = storeService.createStore(mainData);

       if(billId < 0){
           return ErrorMsg("2000","创建失败");
       }

        JSONArray jsonArray = pJson.getJSONArray("linesData");

        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            jsonObject.put("line_number",i);
            jsonObject.put("create_by",userCode);
            jsonObject.put("item_id",i);
            jsonObject.put("header_id",billId);
        }

       boolean isLinesSaveSuccess = invBillLineService.insertBillLines(jsonArray);
        if(!isLinesSaveSuccess){
            return  ErrorMsg("2000","行数据保存失败");
        }

        return SuccessMsg("创建成功",billId);
    }





}
