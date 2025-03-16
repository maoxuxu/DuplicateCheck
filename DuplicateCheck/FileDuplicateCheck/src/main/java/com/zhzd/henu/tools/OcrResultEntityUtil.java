package com.zhzd.henu.tools;

import com.zhzd.henu.entity.OcrResultEntity;

import java.util.*;

public class OcrResultEntityUtil {
    // 将原始实体从第 n 个拆分成两个实体
    // 将原始实体从关键词列表拆分成两个实体
    public static List<OcrResultEntity> splitEntityAt(OcrResultEntity originalEntity, String keywordsList) {
        // 获取原始 keywords Map
        Map<String, OcrResultEntity.Keyword> originalKeywords = originalEntity.getKeywords();

        // 获取 keywordsList 中的关键词（按井号分割）
        String[] keywordsArray = keywordsList.split("#");
        Set<String> keywordSet = new HashSet<>(Arrays.asList(keywordsArray));

        // 创建两个新的 OcrResultEntity 实体
        OcrResultEntity firstEntity = new OcrResultEntity();
        OcrResultEntity secondEntity = new OcrResultEntity();

        // 创建用于存储拆分后的 keywords 的 Map
        Map<String, OcrResultEntity.Keyword> firstKeywords = new LinkedHashMap<>();
        Map<String, OcrResultEntity.Keyword> secondKeywords = new LinkedHashMap<>();

        // 遍历原始 keywords Map，将符合条件的条目放入 firstEntity，其他条目放入 secondEntity
        for (Map.Entry<String, OcrResultEntity.Keyword> entry : originalKeywords.entrySet()) {
            String key = entry.getKey();
            OcrResultEntity.Keyword keyword = entry.getValue();

            // 如果当前条目的 key 在 keywordsList 中，则放入 firstEntity
            if (keywordSet.contains(key)) {
                firstKeywords.put(key, keyword);
            } else {
                secondKeywords.put(key, keyword);
            }
        }

        // 设置两个实体的 keywords
        firstEntity.setKeywords(firstKeywords);
        secondEntity.setKeywords(secondKeywords);

        // 返回包含两个实体的 List
        return Arrays.asList(firstEntity, secondEntity);
    }

}
