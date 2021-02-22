package root.inv.store;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.inv.onhand.InvItemOnHandService;
import root.report.common.DbSession;
import root.report.sys.SysContext;
import root.report.util.DateUtil;
import root.report.util.StringUtil;

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
        int userId = SysContext.getId();
        String orgId = String.valueOf(billLine.get("org_id"));
        String itemId = String.valueOf(billLine.get("item_id"));
        String billType = String.valueOf(billLine.get("bill_type"));
        Object sourceId = billLine.get("source_id");
        Object sourceSystem = billLine.get("source_system");
        Object sourceVoucher =billLine.get("source_voucher");
        Object sourceLineNumber = billLine.get("source_line_number");
        Object headerCreateDate = billLine.get("create_Date");

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
           if(onHandResult.get("on_hand_quantity") != null){
               onHandQuantity =  Double.parseDouble(String.valueOf(onHandResult.get("on_hand_quantity")));
           }
            if(onHandResult.get("price") != null){
                onHandPrice =  Double.parseDouble(String.valueOf(onHandResult.get("price")));
            }
            if(onHandResult.get("amount") != null){
                onHandAmount =  Double.parseDouble(String.valueOf(onHandResult.get("amount")));
            }
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
        transaction.put("begin_price",onHandPrice);
        transaction.put("begin_quantity",onHandQuantity);
        transaction.put("begin_amount",onHandAmount);

        transaction.put("transaction_type_id",isAdd?1:2);
        transaction.put("header_id",String.valueOf(billLine.get("header_id")));
        transaction.put("line_number",String.valueOf(billLine.get("line_number")));
        transaction.put("inv_org_id",orgId);
        transaction.put("item_id",itemId);

        String billTypeName;
        if("store_other".equals(billType)){ //其他入库
            billTypeName= "其他入库";
        }else if("store_po".equals(billType)){ //订单入库
            billTypeName= "订单入库";
        }else if("store_pd".equals(billType)){ //订单入库
            billTypeName= "生产入库";
        }else if("deliver_other".equals(billType)){//出库
            billTypeName= "其他出库";
        }else if("deliver_pd".equals(billType)){//出库
            billTypeName= "生产出库";
        }else if("transfer".equals(billType)){
            billTypeName =  isAdd?"调拨入库":"调拨出库";
        }else if("deliver_sales".equals(billType)){//销售出库
            billTypeName= "销售出库";
        }else if("deliver_wholesales".equals(billType)){//批发出库
            billTypeName =  "批发出库";
        }else{
            billTypeName = "未知类型" + (isAdd?"入库":"出库");
        }
        transaction.put("remark",billTypeName +  billLineQuantity + billLine.get("uom")+ billLine.get("item_description"));

        transaction.put("transaction_quantity",billLineQuantity);
        transaction.put("transaction_price",billLinePrice);
        transaction.put("transaction_amount",billLineAmount);
        transaction.put("uom",String.valueOf(billLine.get("uom")));

        transaction.put("create_by",userId);
        transaction.put("create_date", DateUtil.getCurrentTimm());

        transaction.put("source_id",sourceId);
        transaction.put("source_system",sourceSystem);
        transaction.put("source_voucher",sourceVoucher);
        transaction.put("source_line_number",sourceLineNumber);
        transaction.put("transaction_date",headerCreateDate);

        sqlSession.insert("inv_item_transaction.saveItemTransaction",transaction);

        //更新库存表
        if(hasOnHand){
            invItemOnHandService.updateItemOnHand(sqlSession,onHandResult);
        }else{
            invItemOnHandService.saveItemOnHand(sqlSession,onHandResult);
        }

    }

    /**
     * 加权平均计算
     * @param sqlSession
     * @param billLine 单行信息
     * @param isAdd 入 或者 出 ，入为增加 ，出为减少
     */
    public void weightedMeanForSales(SqlSession sqlSession ,Map<String,Object> billLine,boolean isAdd){
        int userId = SysContext.getId();
        String orgId = String.valueOf(billLine.get("org_id"));
        String itemId = String.valueOf(billLine.get("item_id"));
        String billType = String.valueOf(billLine.get("so_type"));
//        Object sourceId = billLine.get("source_id");
//        Object sourceSystem = billLine.get("source_system");
//        Object sourceVoucher =billLine.get("source_voucher");
//        Object sourceLineNumber = billLine.get("source_line_number");
        Object headerCreateDate = billLine.get("create_Date");

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
            if(onHandResult.get("on_hand_quantity") != null){
                onHandQuantity =  Double.parseDouble(String.valueOf(onHandResult.get("on_hand_quantity")));
            }
            if(onHandResult.get("price") != null){
                onHandPrice =  Double.parseDouble(String.valueOf(onHandResult.get("price")));
            }
            if(onHandResult.get("amount") != null){
                onHandAmount =  Double.parseDouble(String.valueOf(onHandResult.get("amount")));
            }
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
        transaction.put("begin_price",onHandPrice);
        transaction.put("begin_quantity",onHandQuantity);
        transaction.put("begin_amount",onHandAmount);

        transaction.put("transaction_type_id",isAdd?1:2);
        transaction.put("header_id",String.valueOf(billLine.get("header_id")));
        transaction.put("line_number",String.valueOf(billLine.get("line_number")));
        transaction.put("inv_org_id",orgId);
        transaction.put("item_id",itemId);

        String billTypeName;
        if("store_other".equals(billType)){ //其他入库
            billTypeName= "其他入库";
        }else if("store_po".equals(billType)){ //订单入库
            billTypeName= "订单入库";
        }else if("store_pd".equals(billType)){ //订单入库
            billTypeName= "生产入库";
        }else if("deliver_other".equals(billType)){//出库
            billTypeName= "其他出库";
        }else if("deliver_pd".equals(billType)){//出库
            billTypeName= "生产出库";
        }else if("transfer".equals(billType)){
            billTypeName =  isAdd?"调拨入库":"调拨出库";
        }else if("deliver_sales".equals(billType)){//销售出库
            billTypeName= "销售出库";
        }else if("deliver_wholesales".equals(billType)){//批发出库
            billTypeName =  "批发出库";
        }else{
            billTypeName = "未知类型" + (isAdd?"入库":"出库");
        }
        transaction.put("remark",billTypeName +  billLineQuantity + billLine.get("uom")+ billLine.get("item_description"));

        transaction.put("transaction_quantity",billLineQuantity);
        transaction.put("transaction_price",billLinePrice);
        transaction.put("transaction_amount",billLineAmount);
        transaction.put("uom",String.valueOf(billLine.get("uom")));

        transaction.put("create_by",userId);
        transaction.put("create_date", DateUtil.getCurrentTimm());

        transaction.put("source_id",null);
        transaction.put("source_system",null);
        transaction.put("source_voucher",null);
        transaction.put("source_line_number",null);
        transaction.put("transaction_date",headerCreateDate);

        sqlSession.insert("inv_item_transaction.saveItemTransaction",transaction);

        //更新库存表
        if(hasOnHand){
            invItemOnHandService.updateItemOnHand(sqlSession,onHandResult);
        }else{
            invItemOnHandService.saveItemOnHand(sqlSession,onHandResult);
        }

    }

    /**
     * 查询事物 分页
     * @param map
     * @return
     */
    public List<Map<String,Object>> getItemTransactionByPage(Map<String,Object> map){
        return  DbSession.selectList("inv_item_transaction.getItemTransactionByPage",map);
    }

    /**
     * 查询事物 获取数量
     * @param map
     * @return
     */
    public long getItemTransactionByPageCount(Map<String,Object> map){
        return  DbSession.selectOne("inv_item_transaction.getItemTransactionByPageCount",map);
    }

}
