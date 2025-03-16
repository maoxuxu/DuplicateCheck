package com.zhzd.henu.service;

import com.zhzd.henu.entity.OcrResEntity;
import com.zhzd.henu.entity.OcrResultEntity;
import com.zhzd.henu.tools.CheckKeyword;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import static com.zhzd.henu.tools.ConvertStringToIntArray.convertStringToIntArray;
import static com.zhzd.henu.tools.TraverseFolder.traverseFolder;

public class OcrService {

    public static OcrResultEntity processOcr(String imageFolderPath, String scriptPathPdf, String scriptPathImg, String imageFolderPathOut, String keywordsList, String invoiceList, String matchingDegree, String flag, String riskWordsList) throws IOException {
        // 获取只有关键词的长度
        String len;
        if (!keywordsList.equals(null)){
            len = String.valueOf(keywordsList.split("#").length);
        } else{
            len = "0";
        }
        if (keywordsList.equals(invoiceList)){
            len = "0";
        }

//        FileSelectionResultEntity result = selectFiles(new File(imageFolderPath));
        // 存储文件的列表
//        List<File> fileList = new ArrayList<>();
//
//        // 获取文件夹对象
//        File folder = new File(imageFolderPath);
//        // 调用方法遍历文件夹
//        traverseFolder(folder, fileList);

        Map<String, File> fileMap = traverseFolder(new File(imageFolderPath), new File(imageFolderPathOut));
        List<File> fileList = new ArrayList<>(fileMap.values());

        // 创建返回结果对象
        OcrResultEntity ocrResultEntity = new OcrResultEntity();
        ocrResultEntity.setKeywords(new HashMap<>());
        // 创建BingoFile对象
//        // 计算'#'的个数
//        int count = keywordsList.length() - keywordsList.replace("#", "").length();
        // 按 # 进行拆分，存储到数组中
        String[] keywordsArray = keywordsList.split("#");
        String[] riskWordsArray = riskWordsList.split("#");
        for (int j = 0; j < keywordsArray.length; j++) {
            OcrResultEntity.Keyword keyword = new OcrResultEntity.Keyword();
            List<OcrResultEntity.FileInfo> bingoFile = new ArrayList<>();

            // 设置二级目录
            keyword.setBingoNum(0);
            keyword.setBingoFiles(bingoFile);
            String key = keywordsArray[j];
            // 设置一级目录关键词
            ocrResultEntity.getKeywords().put(key, keyword);
        }

        //返回结果
        LinkedHashMap<String, OcrResEntity> ocrResultsMap = new LinkedHashMap<>();
        OcrResEntity ocrResults;

        // 遍历文件夹下的所有需要查重的文件
        for (File file : fileList) {
            try {
                if (file.isFile()) {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                            fileName.endsWith(".png") || fileName.endsWith(".pdf")) {
                        if (file.length() > 200 * 1024 * 1024) { // 检查文件大小
                            System.err.println("File too large: " + file.getName());
                            continue; // 跳过过大的文件
                        }
                        String output = "";
                        if (fileName.endsWith(".pdf")) {
                            boolean canRead = canReadTextFromPDF(file.getAbsolutePath());
                            if (canRead) {
                                scriptPathPdf = scriptPathPdf.replace("_allgl", "_allgl_gz");
                            }
                            if (flag.equals("1") && canRead) {
                                keywordsList = keywordsList + "#" + invoiceList;
                                // 在这里要重置上边的内容才能生效
                                keywordsArray = keywordsList.split("#");
                                for (int j = 0; j < keywordsArray.length; j++) {
                                    String key = keywordsArray[j];
                                    // 如果已经存在这个关键词，则不进行重复添加
                                    if (!ocrResultEntity.getKeywords().containsKey(key)) {
                                        OcrResultEntity.Keyword keyword = new OcrResultEntity.Keyword();
                                        List<OcrResultEntity.FileInfo> bingoFile = new ArrayList<>();
                                        // 设置二级目录
                                        keyword.setBingoNum(0);
                                        keyword.setBingoFiles(bingoFile);
                                        // 设置一级目录关键词
                                        ocrResultEntity.getKeywords().put(key, keyword);
                                    }
                                }
                            }
                            ocrResults = processPdf(file, file.getName(), scriptPathPdf, imageFolderPathOut, keywordsList, matchingDegree, flag, len, riskWordsArray);
                            int KLength = 0;
                            if (canRead) {
                                scriptPathPdf = scriptPathPdf.replace("_allgl_gz", "_allgl");
                                KLength = keywordsList.length()-keywordsList.replace("#","").length()+1;
                            }else {
//                                KLength = keywordsList.length()-keywordsList.replace("#","").length()+1;
                                KLength = keywordsArray.length;
                            }
                            if (ocrResults.getBingoKeyNumArray().length != 0) {
                                for (int i = 0; i < KLength; i++) {
                                    if (ocrResults.getBingoKeyNumArray()[i] != 0) {
                                        OcrResultEntity.FileInfo fileInfo = new OcrResultEntity.FileInfo();
                                        fileInfo.setFileName(fileName);
                                        fileInfo.setPage(ocrResults.getPageHashMap().get(String.valueOf(i)));
                                        fileInfo.setAssetDescription(ocrResults.getAssetDescription());
                                        fileInfo.setPersonFillingName(ocrResults.getPersonFillingName());
                                        fileInfo.setRegistrationExpirationDate(ocrResults.getRegistrationExpirationDate());
                                        fileInfo.setRegistrationCertificateFile(ocrResults.getRegistrationCertificateFile());
                                        fileInfo.setConfidence(ocrResults.getConfidence());
                                        fileInfo.setPath(file.getAbsolutePath());
                                        fileInfo.setSimilarScore(ocrResults.getSimilarScore());
                                        fileInfo.setOutPath(ocrResults.getOutPath());

//                                        // 判断是否包含特殊单词
//                                        boolean containsKeywords = CheckKeyword.containsKeywords(keywordsArray[i]);
                                        boolean containsKeywords = false;
                                        if(!ocrResults.getRiskWordsMap().isEmpty()){
                                            for (String keyword : riskWordsArray) {
                                                if (keywordsArray[i].contains(keyword)&&ocrResults.getRiskWordsMap().containsKey(keyword)) {
                                                    containsKeywords = true;
                                                    break; // 找到第一个匹配的关键词就可以停止检查
                                                }else {
                                                    containsKeywords = false;
                                                }
                                            }
                                        }
                                        fileInfo.setContainsKeywords(containsKeywords);
                                        OcrResultEntity.Keyword keyword = ocrResultEntity.getKeywords().get(keywordsArray[i]);
                                        // 放入命中的文件
                                        keyword.getBingoFiles().add(fileInfo);
                                        // 计算命中次数
                                        keyword.setBingoNum((keyword.getBingoNum() + ocrResults.getBingoKeyNumArray()[i]));
                                    }
                                }
                            }
                        } else {
                            ocrResults = processImage(file, file.getName(), scriptPathImg, imageFolderPathOut, keywordsList, matchingDegree);
                            if (ocrResults.getBingoKeyNumArray().length != 0) {
                                for (int i = 0; i < keywordsArray.length; i++) {
                                    if (ocrResults.getBingoKeyNumArray()[i] != 0) {
                                        OcrResultEntity.FileInfo fileInfo = new OcrResultEntity.FileInfo();
                                        fileInfo.setFileName(fileName);
                                        fileInfo.setPage("1");
                                        fileInfo.setRegistrationCertificateFile(ocrResults.getRegistrationCertificateFile());
                                        fileInfo.setAssetDescription(ocrResults.getAssetDescription());
                                        fileInfo.setPersonFillingName(ocrResults.getPersonFillingName());
                                        fileInfo.setRegistrationExpirationDate(ocrResults.getRegistrationExpirationDate());
                                        fileInfo.setConfidence(ocrResults.getConfidence());
                                        fileInfo.setPath(file.getAbsolutePath());
                                        fileInfo.setSimilarScore(ocrResults.getSimilarScore());
                                        fileInfo.setOutPath(ocrResults.getOutPath());
                                        // 判断是否包含特殊单词
//                                        boolean containsKeywords = CheckKeyword.containsKeywords(keywordsArray[i]);
                                        fileInfo.setContainsKeywords(ocrResults.isContainsKeywords());
                                        OcrResultEntity.Keyword keyword = ocrResultEntity.getKeywords().get(keywordsArray[i]);
                                        // 放入命中的文件
//                                    keyword.getBingoFile().getFiles().put(fileName, fileInfo);
                                        keyword.getBingoFiles().add(fileInfo);
                                        // 计算命中次数
                                        keyword.setBingoNum((keyword.getBingoNum() + ocrResults.getBingoKeyNumArray()[i]));
                                    }
                                }
                            }
                        }
                        ocrResultsMap.put(fileName, ocrResults); // 保存OCR结果
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ocrResultEntity;
    }

    // 处理图片文件
    private static OcrResEntity processImage(File imageFile, String fileName, String scriptPath, String imageFolderPathOut, String keywordsList, String matchingDegree) throws IOException, InterruptedException {
        // 构建命令
        List<String> commands = new ArrayList<>();
        commands.add("python");
        commands.add(scriptPath);
        commands.add(imageFile.getAbsolutePath());
        commands.add(fileName);
        commands.add(imageFolderPathOut);
        commands.add(keywordsList);
        commands.add(matchingDegree);
        // 创建进程构建器
        ProcessBuilder processBuilder = new ProcessBuilder(commands);

        // 启动进程
        Process process = processBuilder.start();
        String output = "";
        String confidence = null;
        String assetDescription = "";
        String personFillingName = "";
        String registrationExpirationDate = "";
        HashMap<String, String> pageHashMap = new HashMap<>();
        String page = null;
        String key;
        int[] bingoKeyNumArray = new int[0];
        double score = 0;
        boolean bingoAll = false;
        boolean bingoSingle = true;
        boolean isAssetDescription = false;
        boolean isRegistrationExpirationDate = false;
        boolean isPersonFillingName = false;
        String similarScore = "";
        ArrayList<Double> scores = new ArrayList<>();
        HashMap<String, String> riskWordsMap = null;
        // 获取进程的输入流（Python脚本的输出）
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                // 获取财产描述
                if (line.contains("财产描述")) {
                    isAssetDescription = true;
                    // 附加前边的内容
                    assetDescription = addAssetDescription(lines, assetDescription);
                }
                if (isAssetDescription) {
                    // 如果进入了下一个表格则跳出
                    if (line.matches(".*(财产附件|财产信息附件|jpg|pdf|第1页|第2页|登记证明编号).*")) {
                        isAssetDescription = false;
                    } else {
                        // 截取财产描述文本信息
                        assetDescription = assetDescription + subInformation(line);
                    }
                }
                // 获取登记到期日
                if (line.contains("登记到期日")) {
                    isRegistrationExpirationDate = true;
                }
                if (isRegistrationExpirationDate) {
                    // 如果进入了下一个表格则跳出
                    if (line.contains("初始登记编号") || line.contains("填表人归档号") || line.contains("授权人")) {
                        isRegistrationExpirationDate = false;
                    } else {
                        // 截取登记到期日文本信息
                        registrationExpirationDate = subInformation(line);
                    }
                }
                // 获取填表人名称
                if (line.contains("填表人名称")) {
                    isPersonFillingName = true;
                }
                if (isPersonFillingName) {
                    // 如果进入了下一个表格则跳出
                    if (line.contains("填表人住所")) {
                        isPersonFillingName = false;
                    } else {
                        // 截取填表人名称文本信息
                        personFillingName = subInformation(line);

                    }
                }
                //计算置信度平均值
                if (line.startsWith("score:")) {
                    score = Double.parseDouble(line.substring(6));
                    scores.add(score);
                }
                if (line.startsWith("similar_score:")) {
                    if (!similarScore.isEmpty()) {
                        if (Double.parseDouble(similarScore) < Double.parseDouble(line.substring(14)))
                            similarScore = Double.parseDouble(line.substring(14)) + "";
                    } else {
                        similarScore = Double.parseDouble(line.substring(14)) + "";
                    }
                }
                //判断是否关键词全命中
                if (line.equals("关键词全命中")) {
                    bingoAll = true;
                }
                //判断是否关键词命中
                if (line.equals("关键词未命中")) {
                    bingoSingle = false;
                }
                //获取每个关键词命中次数
                if (line.startsWith("每个关键词命中的次数：")) {
                    String bingoKeyNum = line.substring(line.indexOf("：") + 1);
                    // 调用方法将其转换为 int 数组
                    bingoKeyNumArray = convertStringToIntArray(bingoKeyNum);
                }
                // 假设这是从Python脚本输出的数据
                if (line.startsWith("[[[")) {
                    lines.add(subInformation(line));
                    output = output + subInformation(line);
                }
            }
//            List<JsonObject> jsonObjects = convertToJSON(lines);
//
//            for (JsonObject jsonObject : jsonObjects) {
//                String str = jsonObject.get("text").toString().substring(1, jsonObject.get("text").toString().length() - 1);
//                output = output + str;
//            }
        }

        // 等待进程执行完毕
        int exitCode = process.waitFor();
        System.out.println("-----------------------------------------------image识别完毕-----------------------------------------------");

        // 获取进程的错误流（如果有错误输出）
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);  // 打印错误输出
            }
        }

        if (!scores.isEmpty()) {
            OptionalDouble average = scores.stream()
                    .mapToDouble(i -> i)
                    .average();
            confidence = String.valueOf(average.getAsDouble());
        }
//        判断字数
//        if (scores.isEmpty() && output.length() < 300) {
//            confidence = "0";
//            similarScore = "0";
//            bingoKeyNumArray = convertStringToIntArray("1");
//        }
//        if (!scores.isEmpty()) {
//            confidence = scores.stream()
//                    .map(String::valueOf) // Convert Double to String
//                    .collect(Collectors.joining("")); // Join them into a single string
//        }
//        confidence = firstHalf(confidence);
        return new OcrResEntity(confidence, assetDescription, bingoAll, bingoSingle, imageFile, pageHashMap, bingoKeyNumArray, personFillingName, registrationExpirationDate, similarScore, imageFolderPathOut + fileName, false, riskWordsMap);
    }

    // 处理pdf文件
    private static OcrResEntity processPdf(File pdfFile, String fileName, String scriptPath, String imageFolderPathOut, String keywordsList, String matchingDegree, String flag, String length, String[] riskWordsArray) throws IOException, InterruptedException {
        PDDocument document = PDDocument.load(pdfFile);
        int numberOfPages = document.getNumberOfPages();
        String numberOfPagesStr = Integer.toString(numberOfPages);
        // 构建命令
        List<String> commands = new ArrayList<>();
        commands.add("python");
//        if (isAllDigits(fileName.replace(".pdf",""))&&fileName.length()==24) {
//            scriptPath = scriptPath.replace("_allgl", "_allgl_gz");
//        }
        System.out.println("keywordsList" + keywordsList);
        commands.add(scriptPath);
        commands.add(pdfFile.getAbsolutePath());
        commands.add(numberOfPagesStr);
        commands.add(fileName);
        commands.add(imageFolderPathOut);
        commands.add(keywordsList);
        commands.add(matchingDegree);
        commands.add(flag);
        commands.add(length);
        // 创建进程构建器
        ProcessBuilder processBuilder = new ProcessBuilder(commands);

        // 启动进程
        Process process = processBuilder.start();
        String output = "";
        String confidence = null;
        String assetDescription = "";
        String personFillingName = "";
        String registrationExpirationDate = "";
        double score = 0;
        boolean isAssetDescription = false;
        boolean isPersonFillingName = false;
        boolean isRegistrationExpirationDate = false;
        boolean bingoAll = false;
        boolean bingoSingle = false;
        HashMap<String, String> pageHashMap = new HashMap<>();
        String page = null;
        String key;
        String similarScore = "";
        ArrayList<Double> scores = new ArrayList<>();
        // 获取进程的输入流（Python脚本的输出）
//        long startTime = System.currentTimeMillis();
        int[] bingoKeyNumArray = new int[0];
        boolean containsKeywords = false;
        HashMap<String, String> riskWordsMap = new HashMap<>();
        String tempRisk = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            List<String> lines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.contains("财产描述")) {
                    isAssetDescription = true;
                    // 附加前边的内容
                    assetDescription = addAssetDescription(lines, assetDescription);
                }
                if (isAssetDescription) {
                    // 如果进入了下一个表格则跳出
                    if (line.matches(".*(财产附件|财产信息附件|jpg|pdf|第1页|第2页|登记证明编号).*") && !assetDescription.equals("质押财产描述")) {
                        isAssetDescription = false;
                    } else {
                        // 截取财产描述文本信息
                        List<String> keywords = Arrays.asList("score", "页", "登记证明编号", "<完>", "中征动产融资登记服务有限责任公司");
                        if (keywords.stream().noneMatch(line::contains)) {
                            if (!line.contains("]], ('"))
                                assetDescription = assetDescription + line;
                            else
                                assetDescription = assetDescription + subInformation(line);
                        }
                    }
                }
                System.out.println(line);
                // --------------------------------------判断是否包含特殊单词
                // String[] riskWordsArray = {"所有", "一切", "全部", "未来"};

                for (String keyword : riskWordsArray) {
                    if (line.contains(keyword)) {
                        containsKeywords = true;
                        tempRisk = keyword;
                        break; // 找到第一个匹配的关键词就可以停止检查
                    }else {
                        containsKeywords = false;
                    }
                }
                if (containsKeywords) {
                    containsKeywords = CheckKeyword.containsKeywords(line);
                    if (containsKeywords)
                        riskWordsMap.put(tempRisk, "true");
                } else {
                    containsKeywords = false;
                }


                // 获取登记到期日
                if (line.contains("登记到期日")) {
                    isRegistrationExpirationDate = true;
                }
                if (isRegistrationExpirationDate) {
                    // 如果进入了下一个表格则跳出
                    if (line.contains("初始登记编号") || line.contains("填表人归档号") || line.contains("授权人")) {
                        isRegistrationExpirationDate = false;
                    } else {
                        // 截取登记到期日文本信息
                        if (!line.contains("]], ('"))
                            registrationExpirationDate = line;
                        else
                            registrationExpirationDate = subInformation(line);
                    }
                }
                // 获取填表人名称
                if (line.contains("填表人名称")) {
                    isPersonFillingName = true;
                }
                if (isPersonFillingName) {
                    // 如果进入了下一个表格则跳出
                    if (line.contains("填表人住所")) {
                        isPersonFillingName = false;
                    } else {
                        if (!line.contains("]], ('"))
                            personFillingName = line;
                        else
                            personFillingName = subInformation(line);
                    }
                }

                //计算置信度平均值
                if (line.startsWith("score:")) {
                    score = Double.parseDouble(line.substring(6));
                    scores.add(score);
                }
                if (line.startsWith("similar_score:")) {
                    if (!similarScore.isEmpty()) {
                        if (Double.parseDouble(similarScore) < Double.parseDouble(line.substring(14)))
                            similarScore = Double.parseDouble(line.substring(14)) + "";
                    } else {
                        similarScore = Double.parseDouble(line.substring(14)) + "";
                    }
                }
                //判断是否关键词全命中
                if (line.equals("关键词全命中")) {
                    bingoAll = true;
                }
                //判断是否关键词命中
                if (line.startsWith("关键词未命中")) {
                    bingoSingle = true;
                }
                //获取每个关键词命中次数
                if (line.startsWith("每个关键词命中的次数：")) {
                    String bingoKeyNum = line.substring(line.indexOf("：") + 1).replace(".0", "").replace(".5", "");
                    // 调用方法将其转换为 int 数组
                    bingoKeyNumArray = convertStringToIntArray(bingoKeyNum);
                }
                // 获取关键词命中的页数
                if (line.startsWith("关键词在某页命中")) {
                    key = line.substring(line.indexOf("，关键词") + 4, line.indexOf("，页数"));
                    page = line.substring(line.indexOf("页数：") + 3);
                    // 如果不为空则追加页数
                    if (pageHashMap.get(key) != null) {
                        pageHashMap.put(key, pageHashMap.get(key) + "," + page);
                    } else {
                        pageHashMap.put(key, page);
                    }
                }
                // 获取识别的结果
                if (line.startsWith("[[[")) {
//                    lines.add(subInformation(line));
                    output = output + subInformation(line);
                }

            }
//            List<JsonObject> jsonObjects = convertToJSON(lines);
//            for (JsonObject jsonObject : jsonObjects) {
//                String str = jsonObject.get("text").toString().substring(1, jsonObject.get("text").toString().length() - 1);
//                output = output + str;
//            }
        }
//        // 记录程序结束时间
//        long endTime = System.currentTimeMillis();
//        // 计算程序运行时间
//        long duration = endTime - startTime;
//        System.out.println("程序运行时间: " + duration + " 毫秒");
        // 等待进程执行完毕
        int exitCode = process.waitFor();
        System.out.println("-----------------------------------------pdf识别完毕---------------------------------------------");

        // 获取进程的错误流（如果有错误输出）
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);  // 打印错误输出
            }
        }
        if (!scores.isEmpty()) {
            OptionalDouble average = scores.stream()
                    .mapToDouble(i -> i)
                    .average();
            confidence = String.valueOf(average.getAsDouble());
        }
        //判断字数
//        if (scores.isEmpty() && output.length() < numberOfPages * 300 && !scriptPath.endsWith("gz.py")) {
//            confidence = "0";
//            similarScore = "0";
//            bingoKeyNumArray = convertStringToIntArray("1");
//        }

//        if (!scores.isEmpty()) {
//            confidence = scores.stream()
//                    .map(String::valueOf) // Convert Double to String
//                    .collect(Collectors.joining(",")); // Join them into a single string
//        }

//        similarScore = removeLastComma(similarScore);
//        confidence = firstHalf(confidence);

        return new OcrResEntity(confidence, assetDescription, bingoAll, bingoSingle, pdfFile, pageHashMap, bingoKeyNumArray, personFillingName, registrationExpirationDate, similarScore, imageFolderPathOut + fileName, containsKeywords, riskWordsMap);
    }


    private static String addAssetDescription(List<String> lines, String assetDescription) {
        // 附加前边的内容
        int currentLineIndex = lines.size();  // 当前行的索引
        while (currentLineIndex > 0) {
            // 获取前一行内容
            String previousLine = lines.get(currentLineIndex - 1);
            if (previousLine.length() > 15 && !previousLine.contains("登记证明编号") && !previousLine.contains("财产价值")) {
                assetDescription = previousLine + assetDescription;
                currentLineIndex--;
            } else {
                break;
            }
        }
        return assetDescription;
    }


    private static String subInformation(String line) {
        // 截取财产描述文本信息
        int startIndex = line.indexOf("]], ('") + 6;
        int endIndex = line.indexOf("', 0");
        if (startIndex >= 0 && endIndex <= line.length() && startIndex <= endIndex && !line.contains("财产描述")) {
            return line.substring(startIndex, endIndex);
        }
        return "";
    }

    public static String removeLastComma(String input) {
        // 检查是否以句号开头
        if (input.startsWith(",")) {
            input = input.substring(1);  // 移除开头的句号
        }
        // 检查是否以句号结尾
        if (input.endsWith(",")) {
            input = input.substring(0, input.length() - 1);  // 移除结尾的句号
        }
        return input;
    }

    public static boolean isAllDigits(String filename) {
        // 检查文件名中的每个字符是否都是数字
        for (int i = 0; i < filename.length(); i++) {
            if (!Character.isDigit(filename.charAt(i))) {
                return false;  // 一旦找到非数字字符，返回 false
            }
        }
        return true;  // 所有字符都是数字，返回 true
    }

    public static boolean canReadTextFromPDF(String pdfFilePath) {
        File pdfFile = new File(pdfFilePath);
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            // 判断提取的文本是否为空
            return text != null && !text.trim().isEmpty();
        } catch (IOException e) {
            System.err.println("Error processing PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
