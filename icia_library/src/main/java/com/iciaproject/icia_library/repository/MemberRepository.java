package com.iciaproject.icia_library.repository;

import com.iciaproject.icia_library.entity.Member;
import org.springframework.data.repository.CrudRepository;

public interface MemberRepository
        extends CrudRepository<Member, String> {
    String findMemberByMpwd(String mid);

    Member findByMid(String mid);
}
