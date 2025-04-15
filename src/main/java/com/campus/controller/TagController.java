package com.campus.controller;

import com.campus.entity.Tag;
import com.campus.service.TagService;
import com.campus.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 标签控制器
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 获取标签列表
     * @return 标签列表
     */
    @GetMapping("/list")
    public List<Tag> getTagList() {
        return tagService.getAllTags();
    }

    /**
     * 根据ID获取标签
     * @param id 标签ID
     * @return 标签对象
     */
    @GetMapping("/get")
    public Tag getTagById(
            @RequestParam("id") Integer id) {
        return tagService.getTagById(id);
    }

    /**
     * 根据帖子ID获取标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    @GetMapping("/getByPostId")
    public List<Tag> getTagsByPostId(
            @RequestParam("postId") Integer postId) {
        return tagService.getTagsByPostId(postId);
    }

    /**
     * 搜索标签
     * @param keyword 关键词
     * @return 标签列表
     */
    @GetMapping("/search")
    public List<Tag> searchTags(
            @RequestParam("keyword") String keyword) {
        // TODO: 实现搜索功能
        // 临时实现：从所有标签中筛选出包含关键词的标签
        List<Tag> allTags = tagService.getAllTags();
        List<Tag> result = new ArrayList<>();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return allTags;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        for (Tag tag : allTags) {
            if (tag.getName().toLowerCase().contains(lowerKeyword)) {
                result.add(tag);
            }
        }
        
        return result;
    }

    /**
     * 添加标签
     * @param tag 标签对象
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result addTag(
            @RequestBody Tag tag) {
        // 设置创建时间和更新时间
        tag.setCreateTime(new Date());
        tag.setUpdateTime(new Date());
        
        boolean success = tagService.addTag(tag);
        
        if (success) {
            return Result.success("标签添加成功");
        } else {
            return Result.error("标签添加失败");
        }
    }

    /**
     * 更新标签
     * @param tag 标签对象
     * @return 更新结果
     */
    @PostMapping("/update")
    public Result updateTag(
            @RequestBody Tag tag) {
        // 设置更新时间
        tag.setUpdateTime(new Date());
        
        boolean success = tagService.updateTag(tag);
        
        if (success) {
            return Result.success("标签更新成功");
        } else {
            return Result.error("标签更新失败");
        }
    }

    /**
     * 删除标签
     * @param id 标签ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result deleteTag(
            @RequestParam("id") Integer id) {
        boolean success = tagService.deleteTag(id);
        
        if (success) {
            return Result.success("标签删除成功");
        } else {
            return Result.error("标签删除失败");
        }
    }

    /**
     * 批量删除标签
     * @param ids 标签ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batchDelete")
    public Result batchDeleteTags(
            @RequestParam("ids") List<Integer> ids) {
        boolean success = tagService.batchDeleteTags(ids);
        
        if (success) {
            return Result.success("标签批量删除成功");
        } else {
            return Result.error("标签批量删除失败");
        }
    }
} 