package sc.hqu.graduationdesign.homeworkmanager.entity;

import lombok.Data;

/**
 * TODO 1.通知加1种类型为短信通知；通知不需要发布数量和已读数量，但是需要确认标识和已确认数量
 *      2.通知可以向班级、学生、家长进行发布；仅向班级发布时可以设置确认标识；
 *      3.除短信通知外，其他类型的通知相当于私信的方式显示在其他端；
 *      4.通知支持重新发送，重新发送时，只需要重新进行推送行为，不需要向数据库保存新纪录
 *      5.通知支持一键提醒，如果是成员通知，那么直接向所有成员群发短信，
 *        如果是班级的通知，默认向班级所有成员发送短信，可选向所有学生家长也发送提醒短信
 *      6.创建通知的界面应该要有一个单选框，向班级发送或向成员发送，
 *        如果选择班级，出现老师管理的所有班级提供复选，
 *        如果选择成员，那么出现所有班级通信录，可以在多个通信录直接进行成员复选，如果不好做那就只支持单个
 *      7.通知发布时，如果不通过文件列表选择附件，那么会默认保存上传的文件
 *        此时上传到服务器的文件会延时进行数据库记录保存，在进行通知发布时才进行文件记录的保存
 *        因为可以在上传文件后取消通知
 *
 * 通知表实体类
 * @author tzx
 * @date 2021-03-14 2:16
 */
@Data
public class NotificationEntity {

    // TODO: 2021/4/3 再去创建一个通知附件的视图，里面保存通知id以及文件名和文件url，通知id不作为主键，主键使用自增主键

    /**
     * 通知id
     */
    private Long id;

    /**
     * 系统账号
     */
    private Long account;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型，0表示普通的文本通知，1表示附件通知，2表示短信通知
     */
    private Integer type;

    /**
     * 通知的成员的类型，可以是学生家长或班级
     */
    private String memberType;

    /**
     * 通知是否需要每个成员都点击确认，默认值是0，代表不需要确认
     * 如果不是向班级发送的通知，不可设置该值
     */
    private Integer confirmable;

    /**
     * 已确认数量，与通知成员数量相对应
     */
    private Integer totalConfirm;

    /**
     * 通知创建日期时间戳
     */
    private Long createDate;

}
