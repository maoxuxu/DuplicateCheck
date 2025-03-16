import io
import os
import sys
from time import sleep
import re
import fitz  # PyMuPDF

from ocr_tools import process_range, calculate_similarity

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
matchingDegree = float(sys.argv[6])
flag = sys.argv[7]
length = float(sys.argv[8])

def add_space_between_characters(s):
    result = []
    result.append(s)
    for i in range(len(s) - 1):
        result.append(s[:i + 1] + ' ' + s[i + 1:])
    return result


def extract_and_annotate_pdf(input_pdf, output_pdf, words_to_highlight):
    # 打开PDF文件
    doc = fitz.open(input_pdf)
    keys = 0  # 用于基数key的数量
    bingo_num = 0  # 用于记录key的命中数量
    bingo_key_num = []  # 用于记录每个关键词命中的次数
    # 遍历每一页并提取文本
    # for word in words_to_highlight:
        # bingo_key_num.append(0)
    bingo_key_num = [0] * len(words_to_highlight)  # 初始化为固定长度的列表

    for page_num in range(len(doc)):
        page = doc.load_page(page_num)  # 加载每一页
        text = page.get_text("text")  # 提取该页的文本
        print(f"\n{text}\n")
        i = -1
        # 为每个需要标注的词查找其位置并高亮显示
        for word in words_to_highlight:
            i = i + 1
            bingo = False
            output_word = add_space_between_characters(word)
            cishu = 0
            temp = ""
            for char in output_word:
                # 使用正则捕获前后8位数字
                match = re.search(r'(\d{8})\s*[-至到]\s*(\d{8})', text)
                if match:
                    # 提取前后8位数字
                    start, end = match.groups()
                    temp = f"{start}-{end}"
                if temp is not None and len(temp) > 5:
                    numbers = process_range(temp)
                    if char in numbers:
                        char = temp
                        temp = ""

                if cishu > 0:
                    # 如果有连字符，可以通过正则表达式匹配去掉连字符
                    char = char.replace("- ", "")  # 去除连字符
                cishu = cishu + 1
                text_instances = page.search_for(char)  # 查找词的位置
                for inst in text_instances:
                    rect = fitz.Rect(inst)  # 获取词的矩形框
                    annot = page.add_rect_annot(rect)  # 添加矩形注释
                    annot.set_colors(stroke=(1, 0, 0))  # 设置填充颜色为黄色
                    annot.set_border(width=2)  # 设置框线的宽度
                    annot.set_opacity(0.5)  # 设置透明度为 50%
                    annot.update()
                    if cishu == 1:
                        bingo_key_num[i] = bingo_key_num[i] + 1
                    else:
                        bingo_key_num[i] = bingo_key_num[i] + 0.5
                    bingo = True
                    print("score:{}".format(1))
                    print("similar_score:{}".format(1))
            if bingo:
                print(f"关键词在某页命中，关键词{i}，页数：{page_num + 1}")
                bingo_num += 1
            else:
                # 如果是关键词则模糊匹配
                if i < length:
                    # 模糊匹配
                    matches = []
                    temp_line = ""
                    for line in text.split("\n"):
                        # 应该滑动窗口模糊匹配了
                        matching_score = calculate_similarity(line, word)
                        if len(line) > len(word):
                            for j in range(len(line) - len(word) + 1):
                                temp_sub_line = line[j:j + len(word)]
                                matching_score = calculate_similarity(temp_sub_line, word)
                                if matching_score > matchingDegree:
                                    matches.append(temp_sub_line)
                                    # TODO
                                    break
                        else:
                            if matching_score <= matchingDegree:
                                matching_score = calculate_similarity(temp_line + line, word)
                            if matching_score > matchingDegree:
                                matches.append(line)
                        temp_line = line
                        # old
                        # matching_score = calculate_similarity(line, word)
                        # if matching_score <= matchingDegree:
                        #     matching_score = calculate_similarity(temp_line + line, word)
                        # if matching_score > matchingDegree:
                        #     matches.append(line)
                        # temp_line = line
                    for match in matches:
                        text_instances = page.search_for(match)  # 查找词的位置
                        for inst in text_instances:
                            rect = fitz.Rect(inst)  # 获取词的矩形框
                            annot = page.add_rect_annot(rect)  # 添加矩形注释
                            annot.set_colors(stroke=(1, 0.647, 0))  # 设置为橙色
                            annot.set_border(width=3)  # 设置框线的宽度
                            # annot.set_opacity(0.9)  # 设置透明度为 50%
                            annot.update()
                            bingo_key_num[i] = bingo_key_num[i] + 1
                            bingo = True
                            print("score:{}".format(matchingDegree))
                            print("similar_score:{}".format(matching_score))
                    if bingo:
                        print(f"关键词在某页命中，关键词{i}，页数：{page_num + 1}")
                        bingo_num += 1

    print(f"每个关键词命中的次数：{bingo_key_num}")

    # 保存已标注的PDF
    doc.save(output_pdf)
    # if os.path.exists(output_pdf):
    #     os.remove(output_pdf)
    # sys.exit()  # 调用sys.exit()彻底退出程序


input_pdf = sys.argv[1]
output_dir = sys.argv[4]
img_name = sys.argv[3]
os.makedirs(output_dir, exist_ok=True)
# output_pdf = output_dir
keyInformationList = sys.argv[5].split("#")
output_path = os.path.join(output_dir, img_name)
try:
    extract_and_annotate_pdf(input_pdf, output_path, keyInformationList)
except Exception as e:
    # 异常被捕获，但不做任何处理，直接忽略
    pass
