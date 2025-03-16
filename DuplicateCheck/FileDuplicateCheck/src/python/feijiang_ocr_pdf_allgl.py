import copy
import fnmatch
import os
import sys
import io
from collections import deque

import numpy as np
import cv2
import fitz
from PIL import Image
from PyPDF2 import PdfWriter, PdfReader
from paddleocr import PaddleOCR, draw_ocr
from ocr_tools import get_color_pdf, process_range, calculate_similarity, chinese_with_punctuation_length, \
    count_non_chinese_symbols, accurate_labeling_pdf
import re
# Initialize PaddleOCR with angle classification and Chinese language support
# ocr = PaddleOCR(use_angle_cls=True, lang="ch", use_gpu=0)

# Redirect stdout and stderr to support UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
os.environ["OMP_NUM_THREADS"] = "8"  # 设置 OpenMP 使用的线程数为 4
os.environ["MKL_NUM_THREADS"] = "8"  # 设置 MKL 使用的线程数为 4
os.environ["CPU_NUM_THREADS"] = "8"  # 设置 CPU 线程数
# File paths
img_path = sys.argv[1]
img_name = sys.argv[3]
length = float(sys.argv[8])

# 要处理的页数
PAGE_NUM = int(sys.argv[2]) # 将识别页码前置作为全局，防止后续打开pdf的参数和前文识别参数不一致 / Set the recognition page number
ocr = PaddleOCR(use_angle_cls=True, lang="ch", page_num=PAGE_NUM, use_gpu=0, show_log=False)
result = ocr.ocr(img_path, cls=False)
# Function to find PDFs

# 输出识别结果
for idx in range(len(result)):
    res = result[idx]
    if res == None: # 识别到空页就跳过，防止程序报错 / Skip when empty result detected to avoid TypeError:NoneType
        print(f"[DEBUG] Empty page {idx+1} detected, skip it.")
        continue
    for line in res:
        print(line)
def find_pdfs(directory, pattern):
    pdf_files = []
    for root, _, files in os.walk(directory):
        for filename in fnmatch.filter(files, pattern):
            if filename.endswith('.pdf'):
                pdf_files.append(os.path.join(root, filename))
    return pdf_files


# 合并PDF的功能
def merge_pdfs(pdf_list, output_path):
    pdf_writer = PdfWriter()
    for pdf in pdf_list:
        pdf_reader = PdfReader(pdf)
        for page_num in range(len(pdf_reader.pages)):
            page = pdf_reader.pages[page_num]
            pdf_writer.add_page(page)
    with open(output_path, 'wb') as output_pdf:
        pdf_writer.write(output_pdf)


# 从PDF中提取图像
imgs = []
with fitz.open(img_path) as pdf:
    for pg in range(0, PAGE_NUM):
        page = pdf[pg]
        mat = fitz.Matrix(2, 2)
        pm = page.get_pixmap(matrix=mat, alpha=False)
        if pm.width > 2000 or pm.height > 2000:
            pm = page.get_pixmap(matrix=fitz.Matrix(1, 1), alpha=False)
        img = Image.frombytes("RGB", [pm.width, pm.height], pm.samples)
        img = cv2.cvtColor(np.array(img), cv2.COLOR_RGB2BGR)
        imgs.append(img)

# 确保输出目录存在
output_dir = sys.argv[4]
os.makedirs(output_dir, exist_ok=True)


keyInformationList = sys.argv[5].split("#")
matchingDegree = float(sys.argv[6])
bingo_key_num = []  # 用于记录每个关键词命中的次数
for keyInformation in keyInformationList:
    bingo_key_num.append(0)

# 处理每个OCR结果
for idx in range(len(result)):
    res = result[idx]
    if res is None:
        continue
    image = imgs[idx]
    boxes = [line[0] for line in res]
    txts = [line[1][0] for line in res]
    scores = [line[1][1] for line in res]

    # 根据分数绘制不同颜色的OCR结果
    temp = None
    keys = 0  # 用于基数key的数量
    bingo_num = 0  # 用于记录key的命中数量

    i = -1

    for keyInformation in keyInformationList:
        i = i + 1
        keys += 1
        bingo = False
        temp = []

        # 创建一个双端队列，最大长度为 3
        recent_info = deque(maxlen=2)
        for original_box, txt, score in zip(boxes, txts, scores):
            color = ""
            similar_score = ""
            # 将新的信息添加到队列
            recent_info.append(txt)
            # 将三条信息合并成一个字符串，没有任何分隔符
            combined_info = "".join(recent_info)
            fancy_hit = False
            box = copy.deepcopy(original_box)
            highlight = False
            numbers = []
            if not isinstance(txt, str):
                txt = str(txt)
            # 确保坐标是整数
            top_left = (int(box[0][0]), int(box[0][1] - 10))

            # 使用正则捕获前后8位数字
            match = re.search(r'(\d{8})\s*[-至到]\s*(\d{8})', txt)
            if match:
                # 提取前后8位数字
                start, end = match.groups()
                temp = f"{start}-{end}"
            if temp is not None and len(temp) > 5:
                numbers = process_range(temp)
                if keyInformation in numbers:
                    # keyInformation = temp
                    fancy_hit = True
                    highlight = True
                    similar_score = 1.0
                    color = get_color_pdf(score, matchingDegree, similar_score)

            if not highlight:
                # 判断颜色和是否命中
                if matchingDegree != 0:
                    # 判断匹配度是否大于50%
                    # 小于的话说明是关键词，不然不是关键词是发票号，所以不需要模糊匹配
                    if i < length:
                        similar_score = calculate_similarity(keyInformation, txt)
                        if similar_score > matchingDegree:
                            highlight = True

                    if keyInformation in txt:
                        similar_score = 1.0
                        highlight = True
                    if keyInformation in combined_info and not any(keyInformation in item for item in recent_info):
                        fancy_hit = True
                        temp = recent_info[1][:len(keyInformation)]
                        similar_score = 1.0
                        highlight = True
                    color = get_color_pdf(score, matchingDegree, similar_score)
                else:
                    similar_score = 1.0
                    color = get_color_pdf(score, matchingDegree, similar_score)
                    if keyInformation in txt:
                        highlight = True
                    if keyInformation in combined_info and not any(keyInformation in item for item in recent_info):
                        fancy_hit = True
                        temp = recent_info[1][:len(keyInformation)]
                        highlight = True

            if highlight:
                bingo_key_num[i] = bingo_key_num[i] + 1
                # 判断是否是特殊标记
                if fancy_hit:
                    image = accurate_labeling_pdf(temp, box, similar_score, txt, image, color)
                    temp = None
                else:
                    image = accurate_labeling_pdf(keyInformation, box, similar_score, txt, image, color)
                bingo = True
                print("score:{}".format(score))
                print("similar_score:{}".format(similar_score))
                # image = cv2.polylines(image, [np.array(box).astype(np.int32)], isClosed=True, color=color, thickness=2)
            # image = cv2.putText(image, txt, top_left, cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
        if bingo:
            print(f"关键词在某页命中，关键词{i}，页数：{idx + 1}")
            bingo_num += 1
    im_show = Image.fromarray(image)

    # 将图像保存到指定目录
    last_dot_index = img_name.rfind('.')
    r = img_name[:last_dot_index]
    output_path = os.path.join(output_dir, r)
    os.makedirs(output_path, exist_ok=True)
    im_show.save("{}/{}.pdf".format(output_path, idx))

    # 输出是否全命中
    if bingo_num == keys:
        print("关键词全命中")
    # 输出是否命中
    if bingo_num == 0:
        print(f"关键词未命中")
    # else:
    #     print(f"关键词命中，页数：{idx+1}")

print(f"每个关键词命中的次数：{bingo_key_num}")
print("...............................................................................q")

# 查找并合并所有输出PDF
pdf_files = find_pdfs(output_path, '*.pdf')
pdf_files.sort()
output_path = os.path.join(output_dir, img_name)
merge_pdfs(pdf_files, output_path)