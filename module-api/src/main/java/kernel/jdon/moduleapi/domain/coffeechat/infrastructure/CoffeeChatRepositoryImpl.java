package kernel.jdon.moduleapi.domain.coffeechat.infrastructure;

import static kernel.jdon.moduledomain.coffeechat.domain.QCoffeeChat.*;
import static kernel.jdon.moduledomain.jobcategory.domain.QJobCategory.*;
import static kernel.jdon.moduledomain.member.domain.QMember.*;
import static org.springframework.util.StringUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import kernel.jdon.moduleapi.domain.coffeechat.core.CoffeeChatCommand;
import kernel.jdon.moduleapi.domain.coffeechat.core.CoffeeChatSortType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoffeeChatRepositoryImpl implements CustomCoffeeChatRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<CoffeeChatReaderInfo.FindCoffeeChatListResponse> findCoffeeChatList(final Pageable pageable,
        final CoffeeChatCommand.FindCoffeeChatListRequest command) {

        List<Long> ids = jpaQueryFactory
            .select(coffeeChat.id)
            .from(coffeeChat)
            .where(
                excludeDeleteCoffeeChat(),
                coffeeChatTitleContains(command.getKeyword()),
                memberJobCategoryEq(command.getJobCategory())
            )
            .orderBy(
                coffeeChatSort(command.getSort())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<CoffeeChatReaderInfo.FindCoffeeChatListResponse> content = jpaQueryFactory
            .select(new QCoffeeChatReaderInfo_FindCoffeeChatListResponse(
                coffeeChat.id,
                member.nickname,
                jobCategory.name,
                coffeeChat.title,
                coffeeChat.coffeeChatStatus,
                coffeeChat.meetDate,
                coffeeChat.createdDate,
                coffeeChat.totalRecruitCount,
                coffeeChat.currentRecruitCount,
                coffeeChat.viewCount))
            .from(coffeeChat)
            .join(member)
            .on(coffeeChat.member.eq(member))
            .join(jobCategory)
            .on(member.jobCategory.eq(jobCategory))
            .where(coffeeChat.id.in(ids))
            .orderBy(
                coffeeChatSort(command.getSort())
            )
            .fetch();

        Long totalCount = jpaQueryFactory
            .select(coffeeChat.count())
            .from(coffeeChat)
            .where(
                excludeDeleteCoffeeChat(),
                coffeeChatTitleContains(command.getKeyword()),
                memberJobCategoryEq(command.getJobCategory())
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, totalCount);
    }

    private OrderSpecifier[] coffeeChatSort(CoffeeChatSortType sort) {
        ArrayList<Object> orderSpecifiers = new ArrayList<>();
        if (CoffeeChatSortType.VIEW_COUNT == sort) {
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, coffeeChat.viewCount));
        } else {
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, coffeeChat.createdDate));
        }

        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }

    private BooleanExpression memberJobCategoryEq(Long jobCategoryId) {
        return jobCategoryId != null ? JPAExpressions
            .selectOne()
            .from(member)
            .where(
                member.id.eq(coffeeChat.member.id),
                member.jobCategory.id.eq(jobCategoryId)
            ).exists() : null;
    }

    private BooleanExpression coffeeChatTitleContains(String keyword) {
        return hasText(keyword) ? coffeeChat.title.contains(keyword) : null;
    }

    private BooleanExpression excludeDeleteCoffeeChat() {
        return coffeeChat.isDeleted.eq(false);
    }

}
