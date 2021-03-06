package sc.hqu.graduationdesign.homeworkmanager.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author tzx
 * @date 2021-03-18 0:56
 */
@AllArgsConstructor
@Getter
public enum ErrorCode {

    /**仅200时为服务成功*/
    SUCCESS(200,"success"),

    /**token鉴权相关的错误*/
    INVALID_TOKEN(400,"凭证无效"),

    /** 系统全局异常错误码*/
    SERVICE_ERROR(500,"服务异常"),
    API_ERROR(501,"接口处理异常"),
    ARGUMENT_ERROR(502,"参数错误"),
    MESSAGE_SENDING_ERROR(510,"短信发送失败"),
    MESSAGE_PUBLISH_ERROR(511,"消息发布失败"),
    LOGIN_SERVICE_ERROR(512,"登录服务异常"),

    USER_NOT_AVAILABLE(600, "用户不可用"),
    USER_NOT_FOUND(601,"用户不存在"),
    DELETE_FORBIDDEN(602,"数据禁止删除"),
    WRONG_VERIFY_CODE(603,"短信验证码错误"),
    VERIFY_CODE_EXPIRED(604,"短信验证码已过期"),
    WRONG_PASS(605,"登录密码错误"),
    INVALID_VERIFY_CODE(606,"验证码无效"),
    WRONG_BIND_PHONE(607,"绑定手机号错误"),
    DUPLICATE_STUDENT(608,"导入的学生中包含已加入当前或其他班级的学生，一个学生只能加入一个班级！");

    private final Integer code;
    private final String desc;

}
