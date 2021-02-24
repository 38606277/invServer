package root.inv.task;


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

import java.sql.SQLException;
import java.util.Map;

/**
 * 付款
 */
@RestController
@RequestMapping(value = "/reportServer/fndTask")
public class FndTaskControl extends RO {

    @Autowired
    FndTaskService bankAccountService;

    @RequestMapping(value = "/getFndTaskListByPage", produces = "text/plain;charset=UTF-8")
    public String getFndTaskListByPage(@RequestBody JSONObject pJson) {
        Map<String,Object> result = bankAccountService.getFndTaskListByPage(pJson);
        return SuccessMsg("", result);
    }

    @RequestMapping(value = "/deleteFndTaskByIds", produces = "text/plain;charset=UTF-8")
    public String deleteFndTaskByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //更新主实体
            bankAccountService.deleteFndTaskByIds(sqlSession,deleteIds);
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
