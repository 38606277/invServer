package root.inv.approval;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.inv.bank.BankAccountService;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.sys.SysContext;
import root.report.util.DateUtil;

import java.sql.SQLException;
import java.util.Map;

/**
 * 审批规则
 */
@RestController
@RequestMapping(value = "/reportServer/approvalRule")
public class ApprovalRuleControl extends RO {

    @Autowired
    ApprovalRuleService approvalRuleService;

    @RequestMapping(value = "/getApprovalRuleListByPage", produces = "text/plain;charset=UTF-8")
    public String getApprovalRuleListByPage(@RequestBody JSONObject pJson) {
        Map<String,Object> result = approvalRuleService.getApprovalRuleListByPage(pJson);
        return SuccessMsg("", result);
    }

    @RequestMapping(value = "/getApprovalRuleListById", produces = "text/plain;charset=UTF-8")
    public String getApprovalRuleListById(@RequestBody JSONObject pJson) {
        Map<String,Object> result = approvalRuleService.getApprovalRuleListById(pJson);
        return SuccessMsg("", result);
    }





    @RequestMapping(value = "/saveApprovalRule", produces = "text/plain; charset=utf-8")
    public String saveApprovalRule(@RequestBody JSONObject pJson) throws SQLException {

        if(approvalRuleService.approvalRuleIsExist(pJson)){
            return ErrorMsg("2000","创建失败,审批关系已存在");
        }

        long id = approvalRuleService.saveApprovalRule(pJson);

        if(id < 0){
            return ErrorMsg("2000","创建失败");
        }
        return SuccessMsg("创建成功",id);

    }

    @RequestMapping(value = "/deleteApprovalRuleByIds", produces = "text/plain;charset=UTF-8")
    public String deleteApprovalRuleByIds(@RequestBody JSONObject pJson)throws SQLException{
        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }
        approvalRuleService.deleteApprovalRuleByIds(pJson);
        return SuccessMsg("删除成功","");

    }

    //更新
    @RequestMapping(value = "/updateApprovalRuleById", produces = "text/plain;charset=UTF-8")
    public String updateApprovalRuleById(@RequestBody JSONObject pJson)throws SQLException {
        String id  = pJson.getString("id");
        if(id ==null || id.isEmpty()){
            return ErrorMsg("保存失败","数据不存在或已删除");
        }
        approvalRuleService.updateApprovalRuleById(pJson);
        return SuccessMsg("保存成功",pJson.get("id"));
    }

}
