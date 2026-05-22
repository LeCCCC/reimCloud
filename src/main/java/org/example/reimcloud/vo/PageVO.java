package org.example.reimcloud.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    /** 总条数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 当前页 */
    private Long current;

    /** 每页大小 */
    private Long size;

    /** 数据集合 */
    private List<T> records;
}
