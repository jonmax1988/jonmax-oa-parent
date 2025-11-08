package com.jonmax.common.result;


import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T date;

    //私有化
    private Result (){

    }
    //返回成功的方法
    public static <T> Result<T> ok(){
        return build(null,ResultCodeEnum.SUCCESS);
    }

    public static <T> Result<T> ok(T date){
        return build(date,ResultCodeEnum.SUCCESS);
    }

    //返回失败的方法
    public static <T> Result<T> fail(){
        return build(null,ResultCodeEnum.FAIL);
    }
    public static <T> Result<T> fail(T date){
        return build(date,ResultCodeEnum.FAIL);
    }

    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }

    //封装返回的数据
    public static <T> Result<T> build(T body,ResultCodeEnum resultCodeEnum){
        Result<T> result = new Result<>();
        if(body !=null ){
            result.setDate(body);
        }
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

}
