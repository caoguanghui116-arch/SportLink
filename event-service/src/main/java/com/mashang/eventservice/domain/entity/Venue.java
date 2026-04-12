package com.mashang.eventservice.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Venue {

  @TableId(type = IdType.AUTO)
  private Long venueId;
  private String venueName;
  private String venueType;
  private Long status;

}
