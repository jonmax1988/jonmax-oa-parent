package com.jonmax.common.config.exception;

import com.jonmax.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {
    //全局异常处理，执行方法
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail().message("执行全局的异常处理。。。");
    }
    //特定的异常处理
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e){
        e.printStackTrace();
        return Result.fail().message("执行特定的异常处理。。。");
    }

    //自定义异常处理
    @ExceptionHandler(JonMaxException.class)
    @ResponseBody
    public Result error(JonMaxException e){
        e.printStackTrace();
        return Result.fail().message(e.getMsg()).code(e.getCode());
    }
}
