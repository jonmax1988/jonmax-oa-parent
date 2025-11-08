package com.jonmax.common.config.exception;

import com.jonmax.common.result.ResultCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class JonMaxException extends RuntimeException{
    private Integer code;
    private String  msg;
    /**
     * 通过状态码和错误消息创建异常对象
     * @param code
     * @param msg
     */
    public JonMaxException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    /**
     * 接收枚举类型对象msg
     * @param resultCodeEnum
     */
    public JonMaxException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMessage();
    }

    @Override
    public String toString() {
        return "JonMaxException{" +
                "code=" + code +
                ", message=" + this.getMsg() +
                '}';
    }
}
