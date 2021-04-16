package root.inv.approval;

import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import root.inv.BaseService;
import root.report.common.DbSession;
import root.report.util.StringUtil;
import sun.rmi.runtime.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批规则
 */
@Service
public class ApprovalRuleService extends BaseService {


    public long saveApprovalRule(Map<String,Object> map){
        int result = DbSession.insert("approval_rule.saveApprovalRule",map);
        if(0 < result){
            return Long.parseLong(String.valueOf(map.get("id")));
        }
        return -1;
    }

    public boolean updateApprovalRuleById(Map<String,Object> map){
        int result = DbSession.update("approval_rule.updateApprovalRuleById",map);
        return  0 < result;
    }

    public boolean deleteApprovalRuleByIds(Map<String,Object> map){
        int result = DbSession.delete("approval_rule.deleteApprovalRuleByIds",map);
        return  0 < result;
    }

    public boolean approvalRuleIsExist(Map<String,Object> map){
        Integer result = DbSession.selectOne("approval_rule.approvalRuleIsExist",map);
        return  0 < result;
    }


    /**
     * 获取审批规则列表
     * @param map
     * @return
     */
    public  Map<String,Object> getApprovalRuleListByPage(Map<String,Object> map){
        return getDataListByPage("approval_rule.getApprovalRuleListByPage",map);
    }

    /**saveApprovalRule
     * 通过id获取审批规则
     * @param map
     * @return
     */
    public  Map<String,Object> getApprovalRuleListById(Map<String,Object> map){
        return DbSession.selectOne("approval_rule.getApprovalRuleListById",map);
    }



    /**
     * 获取审批人
     * @param createUser
     * @param type
     * @return
     */
    public  long  getApprovalUser(String createUser,String type){
        Map<String,Object> map = new HashMap<>();
        map.put("create_user",createUser);
        map.put("type",type);

        List<Map<String,Object>> list = DbSession.selectList("approval_rule.getApprovalUser",map);

        long approvalUserId = -1;
        for(Map<String,Object> item:list ){
           String create_user =  String.valueOf(item.get("create_user"));
             if(createUser.equals(create_user)){
                 String approval_user = String.valueOf(item.get("approval_user"));
                 if(!StringUtil.isEmptyOrNULL(approval_user)){
                     return Long.parseLong(approval_user);
                 }
            }
            if("default".equals(create_user)){
                String approval_user = String.valueOf(item.get("approval_user"));
                if(!StringUtil.isEmptyOrNULL(approval_user)){
                    approvalUserId = Long.parseLong(approval_user);
                }
            }
        }
        return approvalUserId;
    }

    /**
     * 获取默认审批人
     * @param map
     * @return
     */
    public  List<Map<String,Object>> getDefaultApprovalUser(Map<String,Object> map){
        return DbSession.selectList("approval_rule.getDefaultApprovalUser",map);
    }


}
