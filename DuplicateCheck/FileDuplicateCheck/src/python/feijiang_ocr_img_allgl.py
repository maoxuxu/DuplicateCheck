import re
from collections import deque

from paddleocr import PaddleOCR, draw_ocr
import os
from ocr_tools import get_color, process_range, calculate_similarity, accurate_labeling_img

# Initialize PaddleOCR with angle classification and Chinese language support
ocr = PaddleOCR(use_angle_cls=True, lang="ch", use_gpu=0, show_log=False)

import sys
import io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

img_path = sys.argv[1]
img_name = sys.argv[2]
keyInformationList = sys.argv[4].split("#")
matchingDegree = float(sys.argv[5])
result = ocr.ocr(img_path, cls=False)
flag = True

# Print results and check if recognition was successful
for idx in range(len(result)):
    res = result[idx]
    if res:
        flag = True
        for line in res:
            print(line)
    else:
        flag = False
        print("识别失败")

# Display results if recognition was successful
from PIL import Image, ImageDraw

bingo_key_num = []  # 用于记录每个关键词命中的次数
for keyInformation in keyInformationList:
    bingo_key_num.append(0)

if flag:
    result = result[0]
    image = Image.open(img_path).convert('RGB')
    draw = ImageDraw.Draw(image)
    temp = None
    keys = 0  # 用于基数key的数量
    bingo_num = 0  # 用于记录key的命中数量

    i = -1
    for keyInformation in keyInformationList:
        i = i + 1
        keys += 1
        bingo = False
        # 创建一个双端队列，最大长度为 3
        recent_info = deque(maxlen=2)
        for line in result:
            color = ""
            similar_score = ""
            fancy_hit = False
            highlight = False
            box, info = line
            score = info[1]
            # 将新的信息添加到队列
            recent_info.append(info[0])
            # 将三条信息合并成一个字符串，没有任何分隔符
            combined_info = "".join(recent_info)
            # 连号发票处理：
            # 使用正则捕获前后8位数字
            match = re.search(r'(\d{8})\s*[-至到]\s*(\d{8})', info[0])
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
                    color = get_color(score, matchingDegree, similar_score)

            if not highlight:
                # 判断颜色和是否命中
                if matchingDegree != 0:
                    # 判断匹配度是否大于50%
                    similar_score = calculate_similarity(keyInformation, info[0])
                    if similar_score > matchingDegree:
                        highlight = True
                    if keyInformation in info[0]:
                        similar_score = 1.0
                        highlight = True
                    if keyInformation in combined_info and not any(keyInformation in item for item in recent_info):
                        fancy_hit = True
                        temp = recent_info[1][:len(keyInformation)]
                        similar_score = 1.0
                        highlight = True
                    color = get_color(score, matchingDegree, similar_score)
                else:
                    similar_score = 1.0
                    color = get_color(score, matchingDegree, similar_score)
                    if keyInformation in info[0]:
                        highlight = True
                    if keyInformation in combined_info and not any(keyInformation in item for item in recent_info):
                        fancy_hit = True
                        temp = recent_info[1][:len(keyInformation)]
                        highlight = True

            if highlight:
                bingo_key_num[i] = bingo_key_num[i] + 1
                # 判断是否是特殊标记
                if fancy_hit:
                    accurate_labeling_img(temp, box, similar_score, info[0], color, draw)
                    temp = None
                else:
                    accurate_labeling_img(keyInformation, box, similar_score, info[0], color, draw)
                bingo = True
                print("score:{}".format(score))
                print("similar_score:{}".format(similar_score))
                # color = get_color(score)
                box = [tuple(point) for point in box]
                # draw.line([*box, box[0]], fill=color, width=6)

        if bingo:
            bingo_num += 1

    # 输出是否全命中
    if bingo_num == keys:
        print("关键词全命中")
    # 输出是否命中
    if bingo_num == 0:
        print("关键词未命中")
    # Ensure the output directory exists
    output_dir = sys.argv[3]
    os.makedirs(output_dir, exist_ok=True)

    # Save the image to the specified directory
    output_path = os.path.join(output_dir, img_name)
    image.save(output_path)

print(f"每个关键词命中的次数：{bingo_key_num}")