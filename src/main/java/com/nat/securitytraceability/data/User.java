package com.nat.securitytraceability.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户
 * </p>
 *
 * @author hhf
 * @since 2022/4/5
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("admin")
@ApiModel(value="User对象")
public class User implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "自增序列")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "用户登录名")
    private String username;

    @ApiModelProperty(value = "用户密码")
    private String password;

    @ApiModelProperty(value = "用户昵称")
    @TableField(value = "faker_name")
    private String fakerName;

    @ApiModelProperty(value = "用户手机号")
    private String phone;

    @ApiModelProperty(value = "用户电子邮箱")
    private String email;

    @ApiModelProperty(value = "用户地址")
    private String address;

    @ApiModelProperty(value = "用户性别")
    @TableField(value = "user_sex")
    private String userSex;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    public LocalDateTime updateTime;

    @ApiModelProperty(value = "是否已删除 软删除标志位，false：未删除，true:已删除")
    @TableField(value = "delete_flag")
    private String deleteFlag;

}
