# Define color ranges based on scores
import cv2
import numpy as np


def get_color(score, matchingDegree, threshold):
    # if score > 0.999:
    #     return 'green'  # Green for high confidence
    # elif score > 0.95:
    #     return 'blue' # Red for low confidence
    if matchingDegree == 0:
        if score > 0.90:
            return 'red'
        else:
            return 'orange'
    else:
        if threshold >= matchingDegree and score >= 0.90:
            return 'red'
        else:
            return 'orange'


# def get_color_pdf(score, matchingDegree, similar_score):
#     if matchingDegree == 0:
#         if score > 0.90:
#             return (255, 0, 0)
#         else:
#             return (255, 165, 0)
#     else:
#         if similar_score >= matchingDegree and score >= 0.90:
#             return (255, 165, 0)
#         else:
#             return (255, 165, 0)

def get_color_pdf(score, matchingDegree, similar_score):
    if matchingDegree == 0:
        if score > 0.90:
            return (255, 0, 0)
        else:
            return (0, 165, 255)
    else:
        if similar_score == 1:
            return (255, 0, 0)
        else:
            return (0, 165, 255)


# 拆分连号发票
def process_range(range_str):
    result = []

    # Split the range into start and end
    parts = range_str.split('-')
    if len(parts) != 2:
        raise ValueError("Invalid range format")

    start_str, end_str = parts

    # Parse the start and end numbers
    start = int(start_str)
    end = int(end_str)

    # Validate the range
    if start > end:
        raise ValueError("Start number should be less than or equal to end number")

    # Add all numbers in the range to the list
    for i in range(start, end + 1):
        result.append(f"{i:08d}")

    return result


from Levenshtein import distance as levenshtein_distance
import re

# def calculate_similarity(str1, str2):
#     # 计算Levenshtein距离
#     lev_distance = levenshtein_distance(str1, str2)
#
#     # 计算最大长度
#     max_len = max(len(str1), len(str2))
#
#     # 计算相似度
#     similarity = (max_len - lev_distance) / max_len
#
#     return similarity

# 2.22号废弃了
# def calculate_similarity(str1, str2):
#     if ((len(str1) - len(str2)) > 10):
#         return 0
#     # 计算Levenshtein距离
#     lev_distance = levenshtein_distance(str1, str2)
#
#     # 计算最大长度
#     max_len = max(len(str1), len(str2))
#
#     # 计算相似度
#     similarity = (max_len - lev_distance) / max_len
#
#     # 添加对子串相似度的计算
#     max_substring_similarity = 0
#     # 计算所有子串的相似度
#     for i in range(len(str1)):
#         for j in range(i + 1, len(str1) + 1):
#             substring = str1[i:j]
#             sub_lev_distance = levenshtein_distance(substring, str2)
#             sub_similarity = (max(len(substring), len(str2)) - sub_lev_distance) / max(len(substring), len(str2))
#             max_substring_similarity = max(max_substring_similarity, sub_similarity)
#
#     # 返回两者的最大值
#     return max(similarity, max_substring_similarity)

def calculate_similarity(str1, str2):
    # 计算Levenshtein距离
    lev_distance = levenshtein_distance(str1, str2)

    # 计算最大长度
    max_len = max(len(str1), len(str2))

    # 计算相似度
    similarity = (max_len - lev_distance) / max_len

    # 添加对子串相似度的计算
    max_substring_similarity = 0
    # 计算所有子串的相似度
    for i in range(len(str1)):
        for j in range(i + 1, len(str1) + 1):
            substring = str1[i:j]
            sub_lev_distance = levenshtein_distance(substring, str2)
            sub_similarity = (max(len(substring), len(str2)) - sub_lev_distance) / max(len(substring), len(str2))
            max_substring_similarity = max(max_substring_similarity, sub_similarity)

    # 最终相似度加权考虑长度惩罚和子串相似度
    final_similarity = max(similarity, max_substring_similarity)

    # 对长度差距较大的情况进行惩罚，避免相似度过高
    len_diff = abs(len(str1) - len(str2))
    if len_diff > max_len / 2:
        final_similarity *= 0.1  # 可调整惩罚因子的值
    if len(str1) != len(str2):
        final_similarity = final_similarity - len_diff * 0.01

    return final_similarity

if __name__ == '__main__':
    print(calculate_similarity("编号E230807001D的买断型", "号为E230807001D的买断型"))

def chinese_with_punctuation_length(txt):
    # 定义汉字和中文标点符号的正则表达式
    pattern = re.compile(r'[^\d\u0020-\u002F\u003A-\u0040\u005B-\u0060\u007B-\u007F\uFF10-\uFF19]+')
    length = 0

    for char in txt:
        if pattern.match(char):
            length += 1  # 如果是汉字或中文标点符号，长度加 1
        else:
            length += 0.5  # 否则，长度加 0.5
        if char in ["：", "（", "）","【","】"]:
            length += 0.5

    return length

def count_non_chinese_symbols(text):
    # 匹配非汉字和非符号的字符
    non_chinese_symbols = re.findall(r'[^\u4e00-\u9fff\u3000-\u303f\uff00-\uffef“”]', text)
    return len(non_chinese_symbols)


def accurate_labeling_pdf(keyInformation, box, similar_score, txt, image, color):
    # 处理精准标注
    if similar_score == 1.0:
        # if bool(re.fullmatch(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+', txt)):
        key_length = (box[1][0] - box[0][0]) / chinese_with_punctuation_length(txt)
        # key_length =(box[1][0] - box[0][0])/len(txt)
        j = 0
        for char in txt:
            substring = txt[j:j + len(keyInformation)]
            j += 1
            if substring != keyInformation:
                # 是否完全由中文字符（中文标点符号）
                if bool(re.fullmatch(r'[^\d\u0020-\u002F\u003A-\u0040\u005B-\u0060\u007B-\u007F\uFF10-\uFF19]+', substring)):
                    box[0][0] = box[0][0] + key_length
                    box[3][0] = box[3][0] + key_length
                else:
                    box[0][0] = box[0][0] + key_length / 2
                    box[3][0] = box[3][0] + key_length / 2
                if substring and substring[0] in ["：", "（", "）", "【", "】"]:
                    box[0][0] = box[0][0] + key_length / 2
                    box[3][0] = box[3][0] + key_length / 2

            else:
                if bool(re.fullmatch(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+', substring)):
                    box[1][0] = box[0][0] + len(keyInformation) * key_length
                    box[2][0] = box[3][0] + len(keyInformation) * key_length
                else:
                    box[1][0] = box[0][0] + (len(keyInformation) - count_non_chinese_symbols(
                        keyInformation)) * key_length + count_non_chinese_symbols(keyInformation) * key_length / 2
                    box[2][0] = box[3][0] + (len(keyInformation) - count_non_chinese_symbols(
                        keyInformation)) * key_length + count_non_chinese_symbols(keyInformation) * key_length / 2
                # break
                j += len(keyInformation)
                image = cv2.polylines(image, [np.array(box).astype(np.int32)], isClosed=True, color=color,
                                      thickness=2)
                # 上一个标注成功，窗口往后移动一整个关键字
                if bool(re.fullmatch(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+', char)):
                    box[0][0] = box[0][0] + len(keyInformation) * key_length
                    box[3][0] = box[3][0] + len(keyInformation) * key_length
                else:
                    box[0][0] = box[0][0] + (len(keyInformation) - count_non_chinese_symbols(
                        keyInformation)) * key_length + count_non_chinese_symbols(keyInformation) * key_length / 2
                    box[3][0] = box[3][0] + (len(keyInformation) - count_non_chinese_symbols(
                        keyInformation)) * key_length + count_non_chinese_symbols(keyInformation) * key_length / 2

    else:
        image = cv2.polylines(image, [np.array(box).astype(np.int32)], isClosed=True, color=color, thickness=2)
    return image

def accurate_labeling_img(keyInformation, box, similar_score, txt, color, draw):
    # 处理精准标注
    if similar_score == 1.0:
        # if bool(re.fullmatch(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+', txt)):
        key_length = (box[1][0] - box[0][0]) / chinese_with_punctuation_length(txt)
        # key_length =(box[1][0] - box[0][0])/len(txt)
        j = 0
        for char in txt:
            substring = txt[j:j + len(keyInformation)]
            j += 1
            if substring != keyInformation:
                if bool(re.fullmatch(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+', substring)):
                    box[0][0] = box[0][0] + key_length
                    box[3][0] = box[3][0] + key_length
                else:
                    box[0][0] = box[0][0] + key_length / 2
                    box[3][0] = box[3][0] + key_length / 2
            else:
                if bool(re.fullmatch(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+', substring)):
                    box[1][0] = box[0][0] + len(keyInformation) * key_length
                    box[2][0] = box[3][0] + len(keyInformation) * key_length
                else:
                    box[1][0] = box[0][0] + (len(keyInformation) - count_non_chinese_symbols(
                        keyInformation)) * key_length + count_non_chinese_symbols(keyInformation) * key_length / 2
                    box[2][0] = box[3][0] + (len(keyInformation) - count_non_chinese_symbols(
                        keyInformation)) * key_length + count_non_chinese_symbols(keyInformation) * key_length / 2
                # break
                j += len(keyInformation)
                draw.line([int(coord) for point in box for coord in point] + [int(coord) for coord in box[0]], fill=color, width=6)
                # 上一个标注成功，窗口往后移动一整个关键字
                if bool(re.fullmatch(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+', char)):
                    box[0][0] = box[0][0] + len(keyInformation) * key_length
                    box[3][0] = box[3][0] + len(keyInformation) * key_length
                else:
                    box[0][0] = box[0][0] + (len(keyInformation) - count_non_chinese_symbols(
                        keyInformation)) * key_length + count_non_chinese_symbols(keyInformation) * key_length / 2
                    box[3][0] = box[3][0] + (len(keyInformation) - count_non_chinese_symbols(
                        keyInformation)) * key_length + count_non_chinese_symbols(keyInformation) * key_length / 2

    else:
        draw.line([int(coord) for point in box for coord in point] + [int(coord) for coord in box[0]], fill=color, width=6)

