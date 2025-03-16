package com.zhzd.henu.tools;

import com.zhzd.henu.entity.OcrResultEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OcrResultMerger {
    // 合并多个OcrResultEntity中的数据
    public static OcrResultEntity mergeResults(Map<String, OcrResultEntity> fileInfoMap) {
        OcrResultEntity mergedOcrResultEntity = new OcrResultEntity();
        Map<String, OcrResultEntity.Keyword> mergedKeywordsMap = new HashMap<>();

        // 遍历每个OcrResultEntity
        for (OcrResultEntity ocrResultEntity : fileInfoMap.values()) {
            // 遍历该OcrResultEntity中的所有关键词
            for (Map.Entry<String, OcrResultEntity.Keyword> entry : ocrResultEntity.getKeywords().entrySet()) {
                String keywordKey = entry.getKey();
                OcrResultEntity.Keyword keyword = entry.getValue();

                // 如果目标Map中不存在该关键字，则初始化
                OcrResultEntity.Keyword mergedKeyword = mergedKeywordsMap.get(keywordKey);
                if (mergedKeyword == null) {
                    mergedKeyword = new OcrResultEntity.Keyword();
                    mergedKeyword.setBingoNum(0);
                    mergedKeyword.setBingoFiles(new ArrayList<>());
                    mergedKeywordsMap.put(keywordKey, mergedKeyword);
                }

                // 合并该关键字下的文件信息
                for (OcrResultEntity.FileInfo fileInfo : keyword.getBingoFiles()) {
                    mergedKeyword.setBingoNum(mergedKeyword.getBingoNum() + 1);
                    mergedKeyword.getBingoFiles().add(fileInfo);
                }
            }
        }

        // 设置合并后的关键词Map
        mergedOcrResultEntity.setKeywords(mergedKeywordsMap);
        return mergedOcrResultEntity;
    }
}
