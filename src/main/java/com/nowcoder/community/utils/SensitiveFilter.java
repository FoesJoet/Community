package com.nowcoder.community.utils;


import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * Developer：Foes
 */
@Component
public class SensitiveFilter {

    public static final Logger logger =LoggerFactory.getLogger(SensitiveFilter.class);
    private static final String REPLACEMENT="***";
    private TrieNode root =new TrieNode();
    //根据文件初始化前缀树
    @PostConstruct
    public void init(){
        try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while ((keyword =bufferedReader.readLine())!=null){
                    this.addKeyword(keyword);
            }
        }catch (IOException e){
            logger.error("加载敏感词文件失败"+e.getMessage());
        }
    }
    //添加前缀树节点
    private void addKeyword(String keyword) {
        TrieNode tempNode =root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode==null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            tempNode=subNode;
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }

        }
    }
    public String filter(String text){
        TrieNode tempNode =root;
        int begin=0;
        int position=0;
        StringBuilder sb =new StringBuilder();
        while(position<text.length()){
            char c =text.charAt(position);
            //跳过符号
            if(isSymbol(c)){
                if(tempNode==root){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode=tempNode.getSubNode(c);
            //以begin开头的不是敏感词，begin可以存入
            if(tempNode==null){
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=root;
            }else if(tempNode.isKeywordEnd()){
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode=root;
            }else{
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }
    //判断是否为符号
    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }
    //前缀树
    private class TrieNode{

    private boolean isKeywordEnd=false;

    private Map<Character,TrieNode> subNodes =new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
        //添加子节点
        public  void addSubNode(Character c,TrieNode sub){
                subNodes.put(c,sub);
        }
        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
