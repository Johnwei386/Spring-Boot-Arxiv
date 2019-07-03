package com.snail.arxiv.mapper;

import com.snail.arxiv.entity.PaperRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface PaperRecordMapper {

    @Select("select * from records where id=#{id}")
    public PaperRecord getPaperRecordById(String id);

    @Select("select exists(select 1 from records where id=#{id})")
    public Boolean verifyRecordIsExist(String id);

    @Insert("insert into records(id, time, title, summary, authors, link, category) values(#{id}, #{time}, #{title}, #{summary}, #{authors}, #{link}, #{category})")
    public int insertOnePaperRecord(PaperRecord record);

    @Update("update records set time=#{time},title=#{title},summary=#{summary},authors=#{authors},link=#{link},category=#{category} where id=#{id}")
    public int updateOnePaperRecord(PaperRecord record);

    @Select("select exists(select 1 from records)")
    public Boolean verifyTableIsNull();

    @Delete("delete from records")
    public int purgeAllRecords();

    @Select("select * from records order by time desc")
    public List<PaperRecord> getAllPaperRecords();

}
