package com.quokka.classmusic.db.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quokka.classmusic.api.response.TeacherDetailVo;
import com.quokka.classmusic.db.entity.Teacher;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

import static com.quokka.classmusic.db.entity.QContact.contact;
import static com.quokka.classmusic.db.entity.QInstrument.instrument;
import static com.quokka.classmusic.db.entity.QReview.review;
import static com.quokka.classmusic.db.entity.QTeacher.teacher;
import static com.quokka.classmusic.db.entity.QTreat.treat;
import static com.quokka.classmusic.db.entity.QUser.user;

@Repository
public class TeacherRepositoryImpl implements TeacherRepository{
    @PersistenceContext
    private final EntityManager em;
    private final JPAQueryFactory query;

    public TeacherRepositoryImpl(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public TeacherDetailVo findDetailById(int teacherId) {
        return query.select(Projections.constructor(TeacherDetailVo.class ,
                user.name,
                user.gender,
                user.userProfileImage,
                teacher.career,
                teacher.cost,
                teacher.introduce,
                teacher.startTime,
                teacher.endTime,
                teacher.avgRating,
                teacher.contactCnt))
                .from(teacher)
                .where(teacher.teacherId.eq(teacherId))
                .join(teacher.user , user)
                .fetchOne();
    }

    @Override
    public List<Teacher> findAll(Map<String, String> params) {
//        return query.select(teacher)
//                .from(teacher)
//                .where(selectTeacherFilter(params).and(selectTeacherIntroduceFilter(params.get("keyword"))))
//                .join(teacher.user , user)
//                .where(selectGenderFilter(params))
//                .leftJoin(teacher.treats , treat)
//                .leftJoin(treat.instrument , instrument)
//                .where(instrumentEq(params.get("instrument")))
//                .offset((Integer.parseInt(params.get("page")) - 1) * 20)
//                .limit(20)
//                .orderBy(orderType(String.valueOf(params.get("order_by"))))
//                .fetch();
        return query.select(teacher)
                .distinct()
                .from(treat)
                .join(treat.teacher , teacher)
                .join(treat.instrument , instrument)
                .join(teacher.user , user)
                .where(selectTeacherFilter(params).and(selectTeacherIntroduceFilter(params.get("keyword"))))
                .where(selectGenderFilter(params))
                .where(instrumentEq(params.get("instrument")))
                .offset((Integer.parseInt(params.get("page")) - 1) * 20)
                .limit(20)
                .orderBy(orderType(String.valueOf(params.get("orderBy"))))
                .fetch();
    }

    @Override
    public Teacher findById(int teacherId) {
        return em.find(Teacher.class , teacherId);
    }

    @Override
    public void save(Teacher teacher) {
        em.persist(teacher);
    }

    @Override
    public void delete(Teacher teacher) {
        em.remove(teacher);
    }

    @Override
    public long findReviewCount(int teacherId) {
        return query.select(review.count())
                .from(review)
                .join(review.contact , contact)
                .join(contact.teacher , teacher)
                .where(teacher.teacherId.eq(teacherId))
                .fetchOne();
    }

    @Override
    public float findReviewSum(int teacherId) {
        return query.select(review.rating.sum())
                .from(review)
                .join(review.contact , contact)
                .join(contact.teacher , teacher)
                .where(teacher.teacherId.eq(teacherId))
                .fetchOne();
    }

    private BooleanExpression startCareerGoe(Integer startCareer){
        if(startCareer == null){
            return null;
        }
        return teacher.career.goe(startCareer);
    }

    private BooleanExpression endCareerLoe(Integer endCareer){
        if(endCareer == null){
            return null;
        }
        return teacher.career.loe(endCareer);
    }

    private BooleanExpression genderEq(Integer gender){
        if(gender == null){
            return null;
        }
        return user.gender.eq(gender);
    }

    private BooleanExpression startTimeGoe(Integer startTime){
        if(startTime == null){
            return null;
        }
        return teacher.endTime.goe(startTime);
    }

    private BooleanExpression endTimeLoe(Integer endTime){
        if(endTime == null){
            return null;
        }
        return teacher.startTime.loe(endTime);
    }

    private BooleanExpression startCostGoe(Integer startCost){
        if(startCost == null){
            return null;
        }
        return teacher.cost.goe(startCost);
    }

    private BooleanExpression endCostLoe(Integer endCost){
        if(endCost == null){
            return null;
        }
        return teacher.cost.loe(endCost);
    }

    private BooleanExpression instrumentEq(String ins){
        System.out.println(ins + "asdf");
        if(ins.equals("악기종류")){
            return null;
        }
//        return treat.instrument.instrumentName.like("%" + ins + "%");
        return instrument.instrumentName.eq(ins);
    }

    BooleanExpression selectTeacherIntroduceFilter(String keyword){
        if(keyword.equals("")){
            return null;
        }
        return teacher.introduce.like(new StringBuilder().append('%').append(keyword).append('%').toString());
    }

    private BooleanBuilder selectTeacherFilter(Map<String, String> params){
        BooleanBuilder builder = new BooleanBuilder();
        if(!params.get("startCareer").equals("")){
            builder.and(startCareerGoe(Integer.parseInt(String.valueOf(params.get("startCareer")))));
        }
        if(!params.get("endCareer").equals("")){
            builder.and(endCareerLoe(Integer.parseInt(String.valueOf(params.get("endCareer")))));
        }
        if(!params.get("startTime").equals("")){
            builder.and(startTimeGoe(Integer.parseInt(String.valueOf(params.get("startTime")))));
        }
        if(!params.get("endTime").equals("")){
            builder.and(endTimeLoe(Integer.parseInt(String.valueOf(params.get("endTime")))));
        }
        if(!params.get("startCost").equals("")){
            builder.and(startCostGoe(Integer.parseInt(String.valueOf(params.get("startCost")))));
        }
        if(!params.get("endCost").equals("")){
            builder.and(endCostLoe(Integer.parseInt(String.valueOf(params.get("endCost")))));
        }
        return builder;
    }

    private BooleanBuilder selectGenderFilter(Map<String, String> params){
        BooleanBuilder builder = new BooleanBuilder();
        if(!params.get("gender").equals("")){
            builder.and(genderEq(Integer.parseInt(String.valueOf(params.get("gender")))));
        }
        return builder;
    }


    private OrderSpecifier orderType(String orderBy){
        if(orderBy.equals("별점수")){
            return new OrderSpecifier(Order.DESC , teacher.avgRating);
        } else if(orderBy.equals("매칭순")){
            return new OrderSpecifier(Order.DESC , teacher.contactCnt);
        }
        return new OrderSpecifier(Order.DESC , teacher.teacherId);
    }
}
