package com.enterprise.news.bootstrap;

import com.enterprise.news.config.NewsInitProperties;
import com.enterprise.news.model.Post;
import com.enterprise.news.model.PostComment;
import com.enterprise.news.model.TagCategory;
import com.enterprise.news.model.UserProfile;
import com.enterprise.news.repository.PostRepository;
import com.enterprise.news.repository.TagCategoryRepository;
import com.enterprise.news.repository.UserProfileRepository;
import com.enterprise.news.util.PasswordUtils;
import com.enterprise.news.util.UserDefaults;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final NewsInitProperties initProperties;
    private final TagCategoryRepository tagCategoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;

    public DataInitializer(NewsInitProperties initProperties,
                           TagCategoryRepository tagCategoryRepository,
                           UserProfileRepository userProfileRepository,
                           PostRepository postRepository) {
        this.initProperties = initProperties;
        this.tagCategoryRepository = tagCategoryRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) {
        if (!initProperties.isEnabled()) {
            return;
        }
        if (tagCategoryRepository.count() > 0 || userProfileRepository.count() > 0 || postRepository.count() > 0) {
            return;
        }

        TagCategory daily = buildTag("daily", "日常", "生活记录、碎碎念与分享", 1);
        TagCategory tech = buildTag("technology", "科技", "数码、AI、软件与技术体验", 2);
        TagCategory education = buildTag("education", "教育", "学习经验、课程笔记与成长", 3);
        TagCategory sports = buildTag("sports", "体育", "跑步、健身、球类与运动日记", 4);
        TagCategory travel = buildTag("travel", "旅行", "城市漫游与出行攻略", 5);
        tagCategoryRepository.saveAll(List.of(daily, tech, education, sports, travel));

        UserProfile blueberry = new UserProfile();
        blueberry.setUsername("lanmei");
        blueberry.setPasswordHash(PasswordUtils.hash("123456"));
        blueberry.setNickname("蓝莓同学");
        blueberry.setAvatar(UserDefaults.avatar("lanmei"));
        blueberry.setBio("热爱记录生活、拍照和分享学习日常。") ;
        blueberry.setLocation("杭州");
        blueberry.setToken(UUID.randomUUID().toString().replace("-", ""));

        UserProfile anning = new UserProfile();
        anning.setUsername("aning");
        anning.setPasswordHash(PasswordUtils.hash("123456"));
        anning.setNickname("阿宁的慢生活");
        anning.setAvatar(UserDefaults.avatar("aning"));
        anning.setBio("把普通的日子过成发光的样子。") ;
        anning.setLocation("成都");
        anning.setToken(UUID.randomUUID().toString().replace("-", ""));

        userProfileRepository.saveAll(List.of(blueberry, anning));

        Post p1 = new Post();
        p1.setTitle("我的研究生日常工位布置分享");
        p1.setSummary("最近把工位重新整理了一遍，学习氛围感直接拉满，顺手记录一下我的桌面搭配。") ;
        p1.setContent("""
今天终于把工位整理成了我喜欢的样子：

1. 显示器抬高以后，写论文时脖子舒服很多；
2. 台灯换成暖白光，晚上看文献没那么刺眼；
3. 桌面只留常用的键盘、平板和便签，专注感会更强。

如果你也是学生党，真的很推荐先把学习环境收拾好，再去卷效率，体验会完全不一样。""");
        p1.setCoverImage("https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80");
        p1.setImages(List.of(
                "https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80"
        ));
        p1.setTagId(daily.getId());
        p1.setTagCode(daily.getCode());
        p1.setTagName(daily.getName());
        p1.setTopics(List.of("工位改造", "研究生日常", "氛围感"));
        p1.setAuthorId(blueberry.getId());
        p1.setAuthorUsername(blueberry.getUsername());
        p1.setAuthorNickname(blueberry.getNickname());
        p1.setAuthorAvatar(blueberry.getAvatar());
        p1.setViewCount(128L);
        p1.setLikeCount(32L);
        p1.setCommentCount(1);
        p1.setCreatedAt(LocalDateTime.now().minusDays(2));
        PostComment c1 = new PostComment();
        c1.setId(UUID.randomUUID().toString());
        c1.setUserId(anning.getId());
        c1.setNickname(anning.getNickname());
        c1.setAvatar(anning.getAvatar());
        c1.setContent("这个桌面太治愈了，学习效率看起来会很高！");
        c1.setCreatedAt(LocalDateTime.now().minusDays(1));
        p1.setComments(List.of(c1));

        Post p2 = new Post();
        p2.setTitle("周末跑步 5 公里后，我总结了 3 个坚持技巧");
        p2.setSummary("以前总觉得自己坚持不下来，后来把目标变小，反而慢慢跑出了节奏。") ;
        p2.setContent("""
今天周末晨跑了 5 公里，回来以后最大的感受是：坚持运动最重要的不是强度，而是可持续。

我的方法很简单：
- 第一步，先别定太高的目标；
- 第二步，固定每周两次，让身体形成记忆；
- 第三步，跑完给自己一点小奖励。

跑步之后整个人都轻松了很多，下午写代码也更专注。""");
        p2.setCoverImage("https://images.unsplash.com/photo-1552674605-db6ffd4facb5?auto=format&fit=crop&w=1200&q=80");
        p2.setImages(List.of(
                "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?auto=format&fit=crop&w=1200&q=80"
        ));
        p2.setTagId(sports.getId());
        p2.setTagCode(sports.getCode());
        p2.setTagName(sports.getName());
        p2.setTopics(List.of("跑步", "自律", "运动打卡"));
        p2.setAuthorId(anning.getId());
        p2.setAuthorUsername(anning.getUsername());
        p2.setAuthorNickname(anning.getNickname());
        p2.setAuthorAvatar(anning.getAvatar());
        p2.setViewCount(96L);
        p2.setLikeCount(25L);
        p2.setCommentCount(0);
        p2.setCreatedAt(LocalDateTime.now().minusDays(1));
        p2.setComments(List.of());

        Post p3 = new Post();
        p3.setTitle("把 AI 工具接进学习流程后，我的文献整理速度翻倍了");
        p3.setSummary("分享一下我最近用 AI 工具做文献阅读、摘要整理和笔记归档的实际感受。") ;
        p3.setContent("""
最近在写课程论文，我把 AI 工具真正放进了自己的学习流程里：

- 先用它快速提取论文摘要和关键术语；
- 再把重点整理到自己的 markdown 笔记；
- 最后自己复核和补充观点。

它不能替代思考，但真的很适合帮我完成重复整理工作。""");
        p3.setCoverImage("https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&w=1200&q=80");
        p3.setImages(List.of(
                "https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&w=1200&q=80"
        ));
        p3.setTagId(tech.getId());
        p3.setTagCode(tech.getCode());
        p3.setTagName(tech.getName());
        p3.setTopics(List.of("AI学习", "文献阅读", "效率工具"));
        p3.setAuthorId(blueberry.getId());
        p3.setAuthorUsername(blueberry.getUsername());
        p3.setAuthorNickname(blueberry.getNickname());
        p3.setAuthorAvatar(blueberry.getAvatar());
        p3.setViewCount(142L);
        p3.setLikeCount(48L);
        p3.setCommentCount(0);
        p3.setCreatedAt(LocalDateTime.now().minusHours(10));
        p3.setComments(List.of());

        postRepository.saveAll(List.of(p1, p2, p3));
    }

    private TagCategory buildTag(String code, String name, String description, int sort) {
        TagCategory tag = new TagCategory();
        tag.setCode(code);
        tag.setName(name);
        tag.setDescription(description);
        tag.setSort(sort);
        tag.setEnabled(true);
        return tag;
    }
}