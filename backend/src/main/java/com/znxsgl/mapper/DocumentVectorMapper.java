package com.znxsgl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.znxsgl.entity.DocumentVector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentVectorMapper extends BaseMapper<DocumentVector> {

    /** RAG 检索：按课程 + 关键词搜索相关文本块 */
    @Select("SELECT content_chunk FROM document_vector " +
            "WHERE course_name = #{courseName} " +
            "AND (content_chunk LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR doc_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "LIMIT 5")
    List<String> search(@Param("courseName") String courseName,
                        @Param("keyword") String keyword);
}
