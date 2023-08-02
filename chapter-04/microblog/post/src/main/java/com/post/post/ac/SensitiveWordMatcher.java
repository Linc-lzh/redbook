package com.post.post.ac;

import com.post.post.mapper.SensitiveWordMapper;
import com.post.post.model.SensitiveWord;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * 敏感词匹配系统
 * ac自动机
 */
@Component
public class SensitiveWordMatcher {
    private AhoCorasickAutomata aca = new AhoCorasickAutomata();  // Aho-Corasick自动机实例
    private HashMap<String, HashSet<String>> comboMap = new HashMap<>();  // 组合ID与敏感词集合的映射



    // 添加敏感词及其对应的组合ID
    public void addSensitiveWords(String comboID, String[] words) {
        HashSet<String> wordSet = new HashSet<>(Arrays.asList(words));  // 创建敏感词的哈希集合
        comboMap.put(comboID, wordSet);  // 将组合ID与敏感词集合进行映射
        for (String word : words) {
            aca.insert(word, comboID);  // 向Aho-Corasick自动机中插入敏感词
        }
    }

    // 构建Aho-Corasick自动机的失败指针
    public void build() {
        aca.buildFailurePointer();
    }

    // 匹配文本中的敏感词
    public List<String> match(String text) {
        HashMap<String, Boolean> matchResult = aca.match(text);  // 获取匹配结果
        List<String> hitCombos = new ArrayList<>();  // 存储命中的组合ID列表
        for (String comboID : comboMap.keySet()) {
            if (matchResult.getOrDefault(comboID, false)) {
                hitCombos.add(comboID);  // 将匹配到的组合ID添加到结果列表中
            }
        }
        return hitCombos;  // 返回命中的组合ID列表
    }

    public static void main(String[] args) {
        SensitiveWordMatcher swm = new SensitiveWordMatcher();
        swm.addSensitiveWords("Combo1", new String[]{"澳门", "博彩", "网站"});
        swm.addSensitiveWords("Combo2", new String[]{"欢迎", "登录"});
        swm.addSensitiveWords("Combo3", new String[]{"sb"});
        swm.build();

        String text = "欢迎登录澳门XX博彩官方网站";
        List<String> result = swm.match(text);
        for (String comboID : result) {
            System.out.println("Hit combo: " + comboID);
        }
    }

}
