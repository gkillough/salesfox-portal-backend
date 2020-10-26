package ai.salesfox.portal.common.model;

import ai.salesfox.portal.database.account.entity.LoginEntity;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PagedResourceHolderTest {
    @Test
    public void getFirstPageTest() {
        LoginEntity login1 = new LoginEntity(UUID.randomUUID(), "abc", null, null, 0);

        Page<LoginEntity> page = new PageImpl<>(List.of(login1));
        PagedResourceHolder<LoginEntity> pagedResourceHolder = new PagedResourceHolder<>(page, prevPage -> Page.empty());
        assertEquals(page, pagedResourceHolder.getFirstPage());
    }

    @Test
    public void retrieveNextPageTest() {
        LoginEntity login1 = new LoginEntity(UUID.randomUUID(), "abc", null, null, 0);
        LoginEntity login2 = new LoginEntity(UUID.randomUUID(), "abc", null, null, 0);

        Page<LoginEntity> page = new PageImpl<>(List.of(login1, login2));
        PagedResourceHolder<LoginEntity> pagedResourceHolder = new PagedResourceHolder<>(page, prevPage -> Page.empty());

        Page<LoginEntity> nextPage = pagedResourceHolder.retrieveNextPage(page);
        assertEquals(Page.empty(), nextPage);
    }

}
