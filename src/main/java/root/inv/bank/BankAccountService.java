package root.inv.bank;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BankAccountService {

    private static Logger log = Logger.getLogger(BankAccountService.class);

    public  Map<String,Object> getBankAccountListByPage(Map<String,Object> map){
        Map<String,Object> map1=new HashMap<>();

        try {
            RowBounds bounds = null;
            if (map == null) {
                bounds = RowBounds.DEFAULT;
            } else {
                Integer startIndex = Integer.parseInt(map.get("pageNum").toString());
                Integer perPage = Integer.parseInt(String.valueOf(map.get("perPage")));
                if (startIndex == 1 || startIndex == 0) {
                    startIndex = 0;
                } else {
                    startIndex = (startIndex - 1) * perPage;
                }
                bounds = new PageRowBounds(startIndex, perPage);
            }
            List<Map<String, Object>> resultList = DbSession.selectList("mdm_bank_account.getBankAccountListByPage", map, bounds);
            Long totalSize = 0L;
            if (map != null && map.size() != 0) {
                totalSize = ((PageRowBounds) bounds).getTotal();
            } else {
                totalSize = Long.valueOf(resultList.size());
            }

            map1.put("list", resultList);
            map1.put("total", totalSize);

        }catch (Exception e){
            e.printStackTrace();
        }
        return map1;
    }




    public Map<String,Object> getBankAccountById(Map<String,Object> params){
        return DbSession.selectOne("mdm_bank_account.getBankAccountById",params);
    }

    public long saveBankAccount(SqlSession sqlSession,Map<String,Object> params){
        sqlSession.insert("mdm_bank_account.saveBankAccount",params);
        if(params.containsKey("bank_account_id")){
            return  Long.parseLong(params.get("bank_account_id").toString());
        }
        return  -1;
    }

    public void updateBankAccountById(SqlSession sqlSession,JSONObject params){
        sqlSession.update("mdm_bank_account.updateBankAccountById",params);
    }


    public void deleteBankAccountByIds(SqlSession sqlSession,String ids){
        Map<String,String> map = new HashMap<>();
        map.put("ids",ids);
        sqlSession.update("mdm_bank_account.deleteBankAccountByIds",map);
    }

  

}
