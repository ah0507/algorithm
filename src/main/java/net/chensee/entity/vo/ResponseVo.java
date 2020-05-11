package net.chensee.entity.vo;

import lombok.Data;

@Data
public class ResponseVo {

    private int code;

    private Object data;

    private String msg;

    public static ResponseVo ok(Object object){
        ResponseVo responseVo = new ResponseVo();
        responseVo.setCode(1);
        responseVo.setData(object);
        return responseVo;
    }

    public static ResponseVo ok(String msg) {
        ResponseVo responseVo = new ResponseVo();
        responseVo.setCode(1);
        responseVo.setMsg(msg);
        return responseVo;
    }

    public static ResponseVo fail(String msg) {
        ResponseVo responseVo = new ResponseVo();
        responseVo.setCode(0);
        responseVo.setMsg(msg);
        return responseVo;
    }
}
