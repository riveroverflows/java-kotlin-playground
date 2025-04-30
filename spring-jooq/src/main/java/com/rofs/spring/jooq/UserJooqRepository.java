package com.rofs.spring.jooq;

import static com.rofs.jooq.Tables.USERS;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserJooqRepository {

    private final DSLContext dslContext;

    public int save(String identifier) {
        return dslContext.insertInto(USERS)
                         .set(USERS.IDENTIFIER, identifier)
                         .execute();
    }

    public List<User> findAll() {
        return dslContext.selectFrom(USERS)
                         .fetchInto(User.class);
    }

    public User findById(long id) {
        Record record = dslContext.select()
                                  .from(USERS)
                                  .where(USERS.USER_ID.eq(id))
                                  .fetchOne();
        return record == null ? null : User.of(record.get(USERS.USER_ID), record.get(USERS.IDENTIFIER));
    }
}
