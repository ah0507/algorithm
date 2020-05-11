package net.chensee.entity.po.resp;


/**
 * @author ah
 * @title: ObjectResponse
 * @date 2019/10/31 16:05
 */
public class ObjectResponse<T> extends BaseResponse {
    private T data;

    public static ObjectResponse ok() {
        ObjectResponse or = new ObjectResponse();
        or.setCode(ResponseCodeConstants.OK);
        return or;
    }

    public static <T> ObjectResponse ok(T data) {
        ObjectResponse or = new ObjectResponse();
        or.setCode(ResponseCodeConstants.OK);
        or.setData(data);
        return or;
    }

    public static <T> ObjectResponse ok(T data, String msg) {
        ObjectResponse or = new ObjectResponse();
        or.setCode(ResponseCodeConstants.OK);
        or.setData(data);
        or.setMsg(msg);
        return or;
    }

    public static ObjectResponse fail() {
        ObjectResponse or = new ObjectResponse();
        or.setCode(ResponseCodeConstants.FAIL);
        return or;
    }

    public static ObjectResponse fail(Integer code) {
        ObjectResponse or = new ObjectResponse();
        or.setCode(code);
        return or;
    }

    public static ObjectResponse fail(String msg) {
        ObjectResponse or = new ObjectResponse();
        or.setCode(ResponseCodeConstants.FAIL);
        or.setMsg(msg);
        return or;
    }

    public static ObjectResponse fail(Integer code, String msg) {
        ObjectResponse or = new ObjectResponse();
        or.setCode(code);
        or.setMsg(msg);
        return or;
    }

    public static <T> ObjectResponse of(Integer code, T data, String msg) {
        ObjectResponse or = new ObjectResponse();
        or.setCode(code);
        or.setData(data);
        or.setMsg(msg);
        return or;
    }

    public static <T> ObjectResponse of(BaseResponse baseResponse, Object data) {
        ObjectResponse or = new ObjectResponse();
        or.setCode(baseResponse.getCode());
        or.setData(data);
        or.setMsg(baseResponse.getMsg());
        return or;
    }

    public ObjectResponse() {
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

