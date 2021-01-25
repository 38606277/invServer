package root.inv.store;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;

import java.util.List;
import java.util.Map;

/**
 * 事物控制器
 */
@RestController
@RequestMapping(value = "/reportServer/invItemTransaction")
public class InvItemTransactionController extends RO {

    @Autowired
    private InvItemTransactionService invItemTransactionService;

    //查询所有事物
    @RequestMapping(value = "/getItemTransactionListByPage", produces = "text/plain;charset=UTF-8")
    public String getPoListByPage(@RequestBody JSONObject pJson) {

        int currentPage = Integer.valueOf(pJson.getString("pageNum"));
        int perPage = Integer.valueOf(pJson.getString("perPage"));
        if (1 == currentPage || 0 == currentPage) {
            currentPage = 0;
        } else {
            currentPage = (currentPage - 1) * perPage;
        }

        pJson.put("startIndex",currentPage);
        pJson.put("perPage",perPage);

        List<Map<String, Object>> list = invItemTransactionService.getItemTransactionByPage(pJson);
        long total = invItemTransactionService.getItemTransactionByPageCount(pJson);
        return SuccessMsg(list, total);
    }


}
