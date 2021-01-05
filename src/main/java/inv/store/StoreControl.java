package inv.store;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataBarFormatting;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;
import root.configure.AppConstants;
import root.report.common.RO;
import root.report.db.DbFactory;
import root.report.query.FuncMetaData;
import root.report.query.SqlTemplate;
import root.report.service.DictService;
import root.report.service.FunctionService;
import root.report.util.ExecuteSqlUtil;
import root.report.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/reportServer/aa")
public class StoreControl extends RO {



    @Autowired
    public DictService dictService;


    //查询所有的数据字典
    @RequestMapping(value = "/getAllStore", produces = "text/plain;charset=UTF-8")
    public String getAllStore() {

        List<Map<String, String>> list;
        try {
            list = dictService.getAllDictName();
            return SuccessMsg("", list);
        } catch (Exception ex) {
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/getStroeByHeaderId", produces = "text/plain; charset=utf-8")
    public String getStroeByHeaderId(@RequestBody String pJson) {
        Map data = null;
        try {
            data = dictService.getDictValueList(pJson);
            return SuccessMsg("", data);
        } catch (Exception ex) {
            return ExceptionMsg(ex.getMessage());
        }

    }


    /**
     * 功能描述: 接收JSON格式参数，往func_dict跟func_dict_out 中插入相关数据
     */
    @RequestMapping(value = "/createStore", produces = "text/plain;charset=UTF-8")
    public String createStore(@RequestBody String pJson) throws Exception {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            String dict_id = this.dictService.createFuncDict(sqlSession, jsonObject);
            this.dictService.createFuncDictOut(sqlSession, jsonObject, dict_id);
            // 往 数据字典.xml 当中 插入 指定SQL
            // this.dictService.createSqlTemplate("数据字典",dict_id,jsonObject.getString("dict_sql"));

            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("创建字典信息成功", dict_id);
        } catch (Exception ex) {
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    @RequestMapping(value = "/updateStore", produces = "text/plain;charset=UTF-8")
    public String updateStore(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            JSONObject jsonObject = JSON.parseObject(pJson);
            this.dictService.updateFuncDictOut(sqlSession, jsonObject);
            this.dictService.updateFuncDict(sqlSession, jsonObject);

            // 往 数据字典.xml 当中修改 指定SQL
            //  this.dictService.updateSqlTemplate("数据字典",String.valueOf(jsonObject.getIntValue("dict_id")),jsonObject.getString("dict_sql"));

            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("修改字典信息成功", null);
        } catch (Exception ex) {
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        }
    }

    // 从json数据当中解析 ，批量删除 func_dict 表跟 func_dict_out 表当中的数据
    @RequestMapping(value = "/deleteStore", produces = "text/plain;charset=UTF-8")
    public String deleteStore(@RequestBody String pJson) throws SQLException {
        SqlSession sqlSession = DbFactory.Open(DbFactory.FORM);
        try {
            sqlSession.getConnection().setAutoCommit(false);
            JSONArray jsonArray = JSONObject.parseArray(pJson);

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int dict_id = jsonObject.getInteger("dict_id");
                //删除值表，删除out表，删除模板,删除主表，
                this.dictService.deleteDictValueByDictID(sqlSession, dict_id);
                this.dictService.deleteFuncDictOut(sqlSession, dict_id);
                this.dictService.deleteFuncDict(sqlSession, dict_id);
                // 往数据字典.xml 当中删除 指定SQL
                // this.dictService.deleteSqlTemplate("数据字典",String.valueOf(dict_id));
            }

            sqlSession.getConnection().commit();
            // DbFactory.init(DbFactory.FORM);
            return SuccessMsg("删除字典信息成功", null);
        } catch (Exception ex) {
            sqlSession.getConnection().rollback();
            ex.printStackTrace();
            return ExceptionMsg(ex.getMessage());
        } finally {
            sqlSession.getConnection().setAutoCommit(true);
        }
    }



}
