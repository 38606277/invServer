package root.inv.onhand;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;

import java.util.List;
import java.util.Map;

/**
 * 仓库存量
 */
@RestController
@RequestMapping(value = "/reportServer/invOnHand")
public class InvItemOnHandController extends RO {


    @Autowired
    public InvItemOnHandService invItemOnHandService;

    /**
     * 获取物料存量列表
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getItemOnHandByPage", produces = "text/plain;charset=UTF-8")
    public String getItemOnHandByPage(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }

        pJson.put("startIndex",currentPage);
        pJson.put("perPage",perPage);

        List<Map<String, Object>> list = invItemOnHandService.getItemOnHandByPage(pJson);
        long total = invItemOnHandService.getItemOnHandByPageCount(pJson);
        return SuccessMsg(list, total);
    }


    /**
     * 获取仓库存量列表
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getItemOnHandOrgByPage", produces = "text/plain;charset=UTF-8")
    public String getByKeyword(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }

        pJson.put("startIndex",currentPage);
        pJson.put("perPage",perPage);

        List<Map<String, Object>> list = invItemOnHandService.getItemOnHandOrgByPage(pJson);
        long total = invItemOnHandService.getItemOnHandOrgByPageCount(pJson);
        return SuccessMsg(list, total);
    }



    /**
     * 获取仓库类别种类
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getItemOnHandCategoryByPage", produces = "text/plain;charset=UTF-8")
    public String getItemOnHandCategoryByPage(@RequestBody JSONObject pJson) {
        Map map = invItemOnHandService.getItemOnHandCategoryByPage(pJson);
        return SuccessMsg(map);
    }


    /**
     * 获取设置
     * @return
     */
    @RequestMapping(value = "/getItemOnHandSettingByOrgIdAndItemId", produces = "text/plain;charset=UTF-8")
    public String getItemOnHandSettingByOrgIdAndItemId(@RequestBody JSONObject pJson){
        Map<String,Object> data =  invItemOnHandService.getItemOnHandSettingByOrgIdAndItemId(pJson);
        if(data == null){
            return ErrorMsg("2000","数据不存在或已删除");
        }
        return SuccessMsg(data);
    }

    @RequestMapping(value = "/updateItemOnHand", produces = "text/plain;charset=UTF-8")
    public String updateItemOnHand(@RequestBody JSONObject pJson){
        boolean boo  = invItemOnHandService.updateItemOnHand(pJson);
        if(boo){
            return SuccessMsg("更新成功","");
        }
        return ErrorMsg("更新失败");
    }






}
