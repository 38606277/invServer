package root.inv.store;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.sys.SysContext;

import java.util.List;
import java.util.Map;

/**
 * 行数据
 */
@Service
public class InvBillLineService {

    /**
     * 批量插入行数据
     * @param lines
     * @return
     */
    public boolean insertBillLines(List lines){
        if(lines == null || lines.isEmpty()){
            return true;
        }
        // 保存行数据
        int number =  DbSession.insertAll("inv_bill_lines.saveBillLines",lines);
        return  0 < number;
    }

    public List<Map<String,Object>> getBillLinesById(JSONObject jsonObject){
       return  DbSession.selectList("inv_bill_lines.getBillLinesById",jsonObject);
    }

    public long updateBillLines(List<Map<String,Object>> lines){


        return  -1;
    }



    public long deleteBillLines(List<Map<String,Object>> lines){


        return  -1;
    }




}
