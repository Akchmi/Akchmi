package com.quokka.classmusic.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ArticleDto {
    private int articleId;
    private int userId;
    private String title;
    private String content;
    private int createdAt;
    private int hit;
}
