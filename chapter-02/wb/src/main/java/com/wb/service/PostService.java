package com.wb.service;

import com.wb.entity.PostIndex;

import java.util.List;

public interface PostService {

    List<PostIndex> userPosts(int uid,int start,int stop);
}
