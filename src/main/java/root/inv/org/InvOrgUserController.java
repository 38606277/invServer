package root.inv.org;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.DbSession;
import root.report.common.RO;
import root.report.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库用户
 */
@RestController
@RequestMapping(value = "/reportServer/invOrgUser")
public class InvOrgUserController extends RO {


    /**
     * 添加用户
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/add", produces = "text/plain;charset=UTF-8")
    public String add(@RequestBody JSONObject pJson){
        String orgId = pJson.getString("org_id");
        String userIds = pJson.getString("userIds");

        if(StringUtil.isBlank(orgId)){
            return ErrorMsg("2000","仓库不能为空");
        }

        if(StringUtil.isBlank(userIds)){
            return ErrorMsg("2000","请选择需要添加的员工");
        }

        String[] userIdArr = userIds.split(",");
        for(String userId :userIdArr){
            pJson.put("user_id",userId);
            if(isExist(userId)){//判断是否存在
                DbSession.update("inv_org_user.updateByUserId", pJson);
            }else{
                DbSession.insert("inv_org_user.add", pJson);
            }
        }
        return SuccessMsg("保存成功", pJson);
    }


    /**
     * 分页查询仓库员工
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getListByPage", produces = "text/plain;charset=UTF-8")
    public String getListByPage(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }

        pJson.put("startIndex",currentPage);
        pJson.put("perPage",perPage);

        List<Map<String, Object>> list = DbSession.selectList("inv_org_user.getListByPage",pJson);
        Long total = DbSession.selectOne("inv_org_user.getListByPageCount",pJson);
        return SuccessMsg(list, total);
    }

    /**
     * 查询仓库员工
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/getOrgListByUserId", produces = "text/plain;charset=UTF-8")
    public String getOrgListByUserId(@RequestBody JSONObject pJson) {
        List<Map<String, Object>> list = DbSession.selectList("inv_org_user.getOrgListByUserId",pJson);
        return SuccessMsg("",list);
    }


    /**
     * 删除仓库员工
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/deleteByIds", produces = "text/plain;charset=UTF-8")
    public String deleteByUserIds(@RequestBody JSONObject pJson){
        String userIds = pJson.getString("ids");

        if(StringUtil.isBlank(userIds)){
            return ErrorMsg("2000","请选择需要添加的员工");
        }

        DbSession.insert("inv_org_user.deleteByIds", pJson);

        return SuccessMsg("删除成功", pJson);
    }


    private boolean isExist(String userId){
        Map<String,Object> params = new HashMap<>();
        params.put("user_id",userId);
        List<Map<String,Object>> result =  DbSession.selectList("inv_org_user.getOrgUserList",params);
        return result !=null &&  0 < result.size();

    }


}
