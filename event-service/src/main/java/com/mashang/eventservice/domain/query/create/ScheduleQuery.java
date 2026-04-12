package com.mashang.eventservice.domain.query.create;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "赛程信息添加参数")
public class ScheduleQuery {

  @ApiModelProperty(value = "项目id",required = true)
  @NotNull(message = "项目id不能为空")
  private Long itemId;

  @ApiModelProperty(value = "场地id",required = true)
  @NotNull(message = "场地id不能为空")
  private Long venueId;

  @ApiModelProperty(value = "裁判id",required = true)
  @NotNull(message = "裁判id不能为空")
  private Long refereeId;

  @ApiModelProperty(value = "比赛时间",required = true)
  @NotBlank(message = "比赛时间不能为空")
  private Date gameTime;

  @ApiModelProperty(value = "赛程组别",required = true)
  @NotBlank(message = "赛程组别不能为空")
  private String group;

  @ApiModelProperty(value = "状态",required = true)
  @NotNull(message = "状态不能为空")
  private Long status;

}
