package com.example.board;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import com.example.board.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:wordtestdb;DB_CLOSE_DELAY=-1")
class WordReplaceTest {

    @Autowired
    PostService service;

    @Autowired
    PostRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private Post save(String title, String content) {
        Post p = new Post();
        p.setTitle(title);
        p.setName("kim");
        p.setContent(content);
        return repository.save(p);
    }

    @Test
    void 검색에_잡힌_글은_대소문자가_달라도_모두_치환된다() {
        Post p = save("KFC 후기", "오늘 kfc 와 Kfc 를 먹었다");

        List<Long> found = service.searchIdsByWord("kfc");
        int changed = service.replaceWord(found, "kfc", "bhc");

        Post after = repository.findById(p.getId()).orElseThrow();
        assertThat(found).containsExactly(p.getId());
        assertThat(changed).isEqualTo(1);
        assertThat(after.getTitle()).isEqualTo("bhc 후기");
        assertThat(after.getContent()).isEqualTo("오늘 bhc 와 bhc 를 먹었다");
    }

    @Test
    void 특수문자와_치환문자열의_달러_역슬래시는_일반_문자로_처리된다() {
        Post p = save("할인 100% (특가)", "가격 $5 \\ 끝");

        service.replaceWord(List.of(p.getId()), "(특가)", "$1\\");
        service.replaceWord(List.of(p.getId()), "$5", "$9");

        Post after = repository.findById(p.getId()).orElseThrow();
        assertThat(after.getTitle()).isEqualTo("할인 100% $1\\");
        assertThat(after.getContent()).isEqualTo("가격 $9 \\ 끝");
    }

    @Test
    void 없는_id는_건너뛰고_변경_건수에서_제외된다() {
        Post p = save("kfc", "kfc");

        int changed = service.replaceWord(List.of(p.getId(), 9999L), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
    }
}
