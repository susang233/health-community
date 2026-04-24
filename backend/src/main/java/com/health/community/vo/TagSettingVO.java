package com.health.community.vo;

import com.health.community.common.enumeration.TagDisplay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data  // Getter/Setter/ToString/Equals/HashCode
@Builder
@NoArgsConstructor  // 无参构造
@AllArgsConstructor // 全参构造
public class TagSettingVO implements Serializable {

    private TagDisplay display;      // SHOW / HIDE
    private List<String> tags;       // ["学生党", "健身党"]



}
