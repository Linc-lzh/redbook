package com.comment.comment.controller;

import com.comment.comment.model.CommentIndex;
import com.comment.comment.service.CommentService;
import com.common.result.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @RequestMapping("/add")
    public Result add(CommentIndex commentIndex){
        commentService.insert(commentIndex);
        return new Result(200 , "成功" , null);
    }
}
