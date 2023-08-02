package com.post.post.ac;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

class ACNode {
    public boolean isEndingChar = false;  // 是否是模式串的最后一个字符
    public HashMap<Character, ACNode> children = new HashMap<>();  // 子节点的映射表
    public int length = -1;  // 当isEndingChar=true时，记录模式串的长度
    public ACNode fail;  // 失败指针
    public String comboID;  // 添加这一行，用于记录组合ID
}

public class AhoCorasickAutomata {
    private ACNode root = new ACNode();  // 根节点

    /**
     * 插入模式串，并设置对应的组合ID
     *
     * @param text    模式串
     * @param comboID 组合ID
     */
    public void insert(String text, String comboID) {
        ACNode node = root;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (node.children.get(ch) == null) {
                node.children.put(ch, new ACNode());
            }
            node = node.children.get(ch);
        }
        node.isEndingChar = true;
        node.length = text.length();
        node.fail = root;
        node.comboID = comboID;  // 确保节点的comboID设置为参数中的comboID
    }

    /**
     * 构建失败指针
     */
    public void buildFailurePointer() {
        Queue<ACNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            ACNode p = queue.remove();
            for (Character ch : p.children.keySet()) {
                ACNode c = p.children.get(ch);
                if (p == root) {
                    c.fail = root;
                } else {
                    ACNode q = p.fail;
                    while (q != null) {
                        ACNode qc = q.children.get(ch);
                        if (qc != null) {
                            c.fail = qc;
                            break;
                        }
                        q = q.fail;
                    }
                    if (q == null) {
                        c.fail = root;
                    }
                }
                queue.add(c);
            }
        }
    }

    public HashMap<String, Boolean> match(String text) {
        HashMap<String, Boolean> comboResult = new HashMap<>();  // 存储匹配结果的哈希表
        ACNode p = root;  // 当前节点指针，初始化为根节点
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);  // 获取文本中的字符
            while (p.children.get(ch) == null && p != root) {
                p = p.fail;  // 失败指针回溯
            }
            p = p.children.get(ch);  // 获取下一个节点
            if (p == null) {
                p = root;  // 如果节点为空，则将节点指针重置为根节点
            }
            ACNode tmp = p;  // 临时节点指针，用于向上遍历失败指针链
            while (tmp != root) {
                if (tmp.isEndingChar == true) {
                    comboResult.put(tmp.comboID, true);  // 将匹配到的模式串对应的组合ID置为true
                }
                tmp = tmp.fail;  // 失败指针向上遍历
            }
        }
        return comboResult;  // 返回匹配结果
    }
}
