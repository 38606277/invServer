package root.inv.store;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;

import java.util.List;
import java.util.Map;

/**
 * 物料事物记录
 */
@Service
public class InvItemTransactionService {

    /**
     * 批量插入行数据
     * @param lines
     * @return
     */
    public boolean insertBillLinesAll(SqlSession sqlSession ,List lines){
        if(lines == null || lines.isEmpty()){
            return true;
        }
        // 保存行数据
        int number = sqlSession.insert("inv_item_transaction.saveItemTransactionAll",lines);
        return  0 < number;
    }

}
