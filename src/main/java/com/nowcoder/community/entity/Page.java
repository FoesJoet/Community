package com.nowcoder.community.entity;

/**
 * Developer：Foes
 */
public class Page {

    private int current=1;

    private int limit=10;

    private String path;

    private int rows;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>0) {
            this.current = current;
        }else{
            current=1;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>0&&limit<=100) {
            this.limit = limit;
        }else{
            limit=10;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>0) {
            this.rows = rows;
        }
    }
    /**
    获取当前页的起始行
     */
    public int getOffset(){
        return (current-1)*limit;
    }
    /**
    * 获取页数
    * */
    public  int getTotal(){
        if(rows%limit==0){
            return rows/limit;
        }else{
            return rows/limit+1;
        }
    }
    /**
    * 求起始页
    *
    * */
    public int getFrom(){
        int from = current-2;
        return from> 1 ? from : 1;
    }
    /**
     * 求结束页
     *
     * */
    public int getTo(){
        int to = current+2;
        int total =getTotal();
        return to>total?total:to;
    }
}
