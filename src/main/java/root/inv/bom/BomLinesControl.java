package root.inv.bom;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.report.common.RO;

import java.util.List;
import java.util.Map;

/**
 * 产品行
 */
@RestController
@RequestMapping(value = "/reportServer/bomLines")
public class BomLinesControl extends RO {

    @Autowired
    BomLinesService bomLinesService;

    @RequestMapping(value = "/getBomLinesLeafByItemId", produces = "text/plain;charset=UTF-8")
    public String getBomListByPage(@RequestBody JSONObject pJson) {
        List<Map<String,Object>> list =  bomLinesService.getBomLinesLeafByItemId(pJson);
        return SuccessMsg("查询成功",list);
    }

}
