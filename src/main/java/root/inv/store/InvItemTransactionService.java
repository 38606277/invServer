package root.inv.store;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.report.common.DbSession;
import root.report.sys.SysContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物料事物记录
 */
@Service
public class InvItemTransactionService {

    @Autowired
    InvItemOnHandService invItemOnHandService;

    @Autowired
    InvItemTransactionService invItemTransactionService;

    /**
     * 批量插入行数据
     * @param lines
     * @return
     */
    public boolean insertBillLinesAll(SqlSession sqlSession ,List<Map<String,Object>> lines){
        if(lines == null || lines.isEmpty()){
            return true;
        }
        // 保存行数据
        int number = sqlSession.insert("inv_item_transaction.saveItemTransactionAll",lines);
        return  0 < number;
    }


    /**
     * 加权平均计算
     * @param sqlSession
     * @param billLine 单行信息
     * @param isAdd 入 或者 出 ，入为增加 ，出为减少
     */
    public void weightedMean(SqlSession sqlSession ,Map<String,Object> billLine,boolean isAdd){
        String userId = SysContext.getUserId();
        String orgId = String.valueOf(billLine.get("org_id"));
        String itemId = String.valueOf(billLine.get("item_id"));
        String billType = String.valueOf(billLine.get("bill_type"));

        //行信息
        double billLineQuantity = Double.parseDouble(String.valueOf(billLine.get("quantity")));
        double billLinePrice = Double.parseDouble(String.valueOf(billLine.get("price")));
        double billLineAmount = Double.parseDouble(String.valueOf(billLine.get("amount")));

        //库存信息
        Map<String,Object> onHandParams = new HashMap<>();
        onHandParams.put("org_id",billLine.get("org_id"));
        onHandParams.put("item_id",billLine.get("item_id"));
        Map<String,Object> onHandResult =  invItemOnHandService.getItemOnHandByParams(onHandParams);
        double onHandQuantity = 0;
        double onHandPrice = 0;
        double onHandAmount = 0;
        boolean hasOnHand = false;
        if(onHandResult!=null && !onHandResult.isEmpty()){
             hasOnHand = true;
             onHandQuantity = Double.parseDouble(String.valueOf(onHandResult.get("on_hand_quantity")));
             onHandPrice = Double.parseDouble(String.valueOf(onHandResult.get("price")));
             onHandAmount = Double.parseDouble(String.valueOf(onHandResult.get("amount")));
        }else{
            onHandResult = new HashMap<>();
            onHandResult.put("org_id",orgId);
            onHandResult.put("item_id",itemId);
        }

        double quantity;//总数量
        double amount;//总金额
        double price;//单价

        //总数量
        //总金额
        if(isAdd){
             quantity = onHandQuantity + billLineQuantity;//总数量
             amount = onHandAmount + billLineAmount;//总金额
        }else{
             quantity = onHandQuantity - billLineQuantity;//总数量
             amount = onHandAmount - billLineAmount;//总金额
        }
        price = amount/quantity;//单价

        onHandResult.put("on_hand_quantity",quantity);
        onHandResult.put("amount",amount);
        onHandResult.put("price",price);


        //事物
        Map<String,Object> transaction = new HashMap<>();

        transaction.put("transaction_type_id",billType);
        transaction.put("header_id",String.valueOf(billLine.get("header_id")));
        transaction.put("line_number",String.valueOf(billLine.get("line_number")));
        transaction.put("inv_org_id",orgId);
        transaction.put("item_id",itemId);

        String billTypeName;
        if(billType.startsWith("store_other")){ //其他入库
            billTypeName= "其他入库";
        }else if(billType.startsWith("store_po")){ //订单入库
            billTypeName= "订单入库";
        }else if("deliver".equals(billType)){//出库
            billTypeName= isAdd?"调拨入库":"调拨出库";
        }else{
            billTypeName = "未知类型" + (isAdd?"入库":"出库");
        }
        transaction.put("remark",billTypeName +  billLineQuantity + billLine.get("uom")+ billLine.get("item_description"));


        //transaction.put("primary_quantity",quantity);
        transaction.put("transaction_quantity",billLineQuantity);
        transaction.put("transaction_uom",String.valueOf(billLine.get("uom")));
        transaction.put("price",billLinePrice);
        transaction.put("amount",billLineAmount);
        transaction.put("create_by",userId);
        sqlSession.insert("inv_item_transaction.saveItemTransaction",transaction);

        //更新库存表
        if(hasOnHand){
            invItemOnHandService.updateItemOnHand(sqlSession,onHandResult);
        }else{
            invItemOnHandService.saveItemOnHand(sqlSession,onHandResult);
        }



    }

}
