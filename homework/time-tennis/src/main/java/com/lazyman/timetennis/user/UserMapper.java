package com.lazyman.timetennis.user;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_user
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(String openId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_user
     *
     * @mbg.generated
     */
    int insert(User record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_user
     *
     * @mbg.generated
     */
    User selectByPrimaryKey(String openId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_user
     *
     * @mbg.generated
     */
    List<User> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tt_user
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(User record);
}