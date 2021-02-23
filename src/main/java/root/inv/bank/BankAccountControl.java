package root.inv.bank;


import com.alibaba.fastjson.JSONArray;
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
@RequestMapping(value = "/reportServer/bankAccount")
public class BankAccountControl extends RO {

    @Autowired
    BankAccountService bankAccountService;


    @RequestMapping(value = "/getBankAccountListByPage", produces = "text/plain;charset=UTF-8")
    public String getBankAccountListByPage(@RequestBody JSONObject pJson) {
        Map<String,Object> result = bankAccountService.getBankAccountListByPage(pJson);
        return SuccessMsg("", result);
    }

    //查询详情
    @RequestMapping(value = "/getBankAccountById", produces = "text/plain;charset=UTF-8")
    public String getBankAccountById(@RequestBody JSONObject pJson){

        Map<String, Object> mainData = bankAccountService.getBankAccountById(pJson);
        if(mainData == null || mainData.isEmpty()){
            return ErrorMsg("2000","数据不存在");
        }
        return SuccessMsg("获取成功", mainData);
    }
    /**
     * 新增事物
     * @param pJson
     * @return
     */
    @RequestMapping(value = "/createBankAccount", produces = "text/plain; charset=utf-8")
    public String createBankAccount(@RequestBody JSONObject pJson) throws SQLException{
        int userId = SysContext.getId();
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        try {

            sqlSession.getConnection().setAutoCommit(false);

            pJson.put("create_by",userId);
            pJson.put("update_by",userId);
            pJson.put("create_date", DateUtil.getCurrentTimm());
            pJson.put("update_date", DateUtil.getCurrentTimm());

            //保存主数据
            long id = bankAccountService.saveBankAccount(sqlSession,pJson);
            if(id < 0){
                return ErrorMsg("2000","创建失败");
            }

            sqlSession.getConnection().commit();
            return SuccessMsg("创建成功",id);
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

    @RequestMapping(value = "/deleteBankAccountByIds", produces = "text/plain;charset=UTF-8")
    public String deleteBankAccountByIds(@RequestBody JSONObject pJson)throws SQLException{
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);

        String deleteIds  = pJson.getString("ids");
        if(deleteIds ==null || deleteIds.isEmpty()){
            return ErrorMsg("删除失败","请选择删除项");
        }

        try {
            sqlSession.getConnection().setAutoCommit(false);
            //更新主实体
            bankAccountService.deleteBankAccountByIds(sqlSession,deleteIds);
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


    //更新
    @RequestMapping(value = "/updateBankAccountById", produces = "text/plain;charset=UTF-8")
    public String updateBankAccountById(@RequestBody JSONObject pJson)throws SQLException {
        SqlSession sqlSession =  DbFactory.Open(DbFactory.FORM);
        int userId = SysContext.getId();
        try {
            sqlSession.getConnection().setAutoCommit(false);

            bankAccountService.updateBankAccountById(sqlSession,pJson);

            sqlSession.getConnection().commit();
            return SuccessMsg("保存成功",pJson.get("payment_id"));
        } catch (Exception ex){
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }

}
