package root.inv;

import com.github.pagehelper.PageRowBounds;
import org.apache.ibatis.session.RowBounds;
import root.report.common.DbSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础服务
 */
public class BaseService {

    public Map<String,Object> getDataListByPage(String mapper, Map<String,Object> map){
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
            List<Map<String, Object>> resultList = DbSession.selectList(mapper, map, bounds);
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
            map1.put("list", new ArrayList<>());
            map1.put("total", 0);
        }
        return map1;
    }


}
