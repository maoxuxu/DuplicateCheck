package com.zhzd.henu.controller;

import com.zhzd.henu.entity.OcrResultEntity;
import com.zhzd.henu.service.OcrService;
import com.zhzd.henu.tools.OcrResultEntityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javafx.scene.control.Separator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.zhzd.henu.config.GlobalExceptionHandler.requireNonEmpty;

@RestController
@CrossOrigin(origins = "*")
@Api(description = "OCR识别和抽取关键词并标注")
public class PaddleOcrController {
    private static final Logger logger= LoggerFactory.getLogger(PaddleOcrController.class);

    // 定义Python脚本的文件路径
    @Value("${app.scriptPathImg}")
    private String scriptPathImg; // Python脚本路径

    @Value("${app.scriptPathPdf}")
    private String scriptPathPdf; // Python脚本路径

    @PostMapping("/ocr")
    @ApiOperation(value = "传入图片/PDF所在文件夹路径，输出路径，关键词列表（以井号分隔）。以json形式返回每个文件的OCR结果")
    public Map<String, Map<String, OcrResultEntity.Keyword>> ocrImgAndPdf(String inputPath,
                                                                          String outputPath,
                                                                          @RequestParam(value = "keywordsList", defaultValue = "") String keywordsList,
                                                                          @RequestParam(value = "invoiceList", defaultValue = "") String invoiceList,
                                                                          @RequestParam(value = "riskWordsList", defaultValue = "") String riskWordsList,
                                                                          @RequestParam(value = "buyerList", defaultValue = "") String buyerList,
                                                                          String matchingDegree,
                                                                          String riskWords) throws IOException {
//        System.out.println("000000000" + keywordsList);
        logger.info("inputPath,{};outputPath,{},matchingDegree{}",inputPath,outputPath,matchingDegree);
        requireNonEmpty("输入参数不能为空", inputPath, outputPath, matchingDegree);
        if (!outputPath.endsWith(File.separator)) {
            outputPath += File.separator;
        }
        // 判断两个都不为空
        String flag = "0";
        if (!invoiceList.isEmpty()&&!keywordsList.isEmpty()){
            flag = "1";
        }
        // 存放关键词、发票号、危险词三种识别结果
        Map<String, Map<String, OcrResultEntity.Keyword>> ResultEntityMap = new HashMap<>();
        try {
            // 遍历文件夹所有附件并处理：
            if (!keywordsList.equals("")){
                int keywordsListLength = keywordsList.length() - keywordsList.replace("#", "").length() + 1;
                OcrResultEntity ocrResultEntityKeywordsList = OcrService.processOcr(inputPath, scriptPathPdf, scriptPathImg, outputPath, keywordsList,invoiceList, matchingDegree, flag, riskWords);
                // 如果是可读的pdf只能识别一遍 所以要拆分实体
                if (flag == "1" && keywordsListLength < ocrResultEntityKeywordsList.getKeywords().size()){
                    List<OcrResultEntity> ocrResultEntities = OcrResultEntityUtil.splitEntityAt(ocrResultEntityKeywordsList, keywordsList);
                    ocrResultEntities = removeDuplicateFileNames(ocrResultEntities);
                    ResultEntityMap.put("keywords", ocrResultEntities.get(0).getKeywords());
                    ResultEntityMap.put("invoice", ocrResultEntities.get(1).getKeywords());
                    return ResultEntityMap;
                }else {
                    ResultEntityMap.put("keywords", ocrResultEntityKeywordsList.getKeywords());
                }
            }else {
                ResultEntityMap.put("keywords", null);
            }
            if (!invoiceList.equals("") && !keywordsList.equals("")) {
                OcrResultEntity ocrResultEntityInvoiceList = OcrService.processOcr(inputPath, scriptPathPdf, scriptPathImg, outputPath, invoiceList,invoiceList, "0", flag, riskWords);
                ResultEntityMap.put("invoice", ocrResultEntityInvoiceList.getKeywords());
            }
            else if (!invoiceList.equals("")){
                OcrResultEntity ocrResultEntityInvoiceList = OcrService.processOcr(inputPath, scriptPathPdf, scriptPathImg, outputPath, invoiceList,invoiceList, "0", flag, riskWords);
                ResultEntityMap.put("invoice", ocrResultEntityInvoiceList.getKeywords());
            }else ResultEntityMap.put("invoice", null);

            if (!riskWordsList.equals("")){
                OcrResultEntity ocrResultEntityRiskWordsList = OcrService.processOcr(inputPath, scriptPathPdf, scriptPathImg, outputPath + "riskWords\\", riskWordsList,invoiceList, "0", flag, riskWords);
                ResultEntityMap.put("riskWords", ocrResultEntityRiskWordsList.getKeywords());
            }else ResultEntityMap.put("riskWords", null);

            if (!buyerList.equals("")){
                OcrResultEntity ocrResultEntityRiskWordsList = OcrService.processOcr(inputPath, scriptPathPdf, scriptPathImg, outputPath + "buyer\\", riskWordsList,invoiceList, "0", flag, riskWords);
                ResultEntityMap.put("buyerList", ocrResultEntityRiskWordsList.getKeywords());
            }else ResultEntityMap.put("buyerList", null);

        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException("空指针异常");
        }

        return ResultEntityMap;
    }


    // 去除重复的文件信息
    // 去除同一个文件名重复的实体，并更新 bingoNum
    public static List<OcrResultEntity> removeDuplicateFileNames(List<OcrResultEntity> entities) {
        for (OcrResultEntity entity : entities) {
            Map<String, OcrResultEntity.Keyword> keywords = entity.getKeywords();
            for (Map.Entry<String, OcrResultEntity.Keyword> entry : keywords.entrySet()) {
                OcrResultEntity.Keyword keyword = entry.getValue();
                List<OcrResultEntity.FileInfo> uniqueFiles = new ArrayList<>();
                Set<String> seenFileNames = new HashSet<>();
                int removedCount = 0;  // 用来记录去掉的重复文件数量

                // 遍历文件列表，去除重复的文件并更新 bingoNum
                for (OcrResultEntity.FileInfo file : keyword.getBingoFiles()) {
                    String fileName = file.getFileName();
                    if (!seenFileNames.contains(fileName)) {
                        uniqueFiles.add(file);
                        seenFileNames.add(fileName);
                    } else {
                        removedCount++;  // 计数已去除的重复项
                    }
                }

                // 更新去重后的文件列表
                keyword.setBingoFiles(uniqueFiles);
                keyword.setBingoNum(keyword.getBingoNum() - removedCount);  // 更新 bingoNum
            }
        }
        return entities;
    }
}



