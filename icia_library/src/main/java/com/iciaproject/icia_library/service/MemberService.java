package com.iciaproject.icia_library.service;

import com.iciaproject.icia_library.entity.Board;
import com.iciaproject.icia_library.entity.Book;
import com.iciaproject.icia_library.entity.Member;
import com.iciaproject.icia_library.repository.BoardRepository;
import com.iciaproject.icia_library.repository.BookRepository;
import com.iciaproject.icia_library.repository.MemberRepository;
import com.iciaproject.icia_library.util.PagingUtil;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;


import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Log
public class MemberService {
    @Autowired
    public MemberRepository mRepo;

    @Autowired
    private BookRepository bRepo;

    @Autowired
    private BoardRepository boRepo;


//    @Autowired
//    private BoardFileRepository bfRepo;

    ModelAndView mv;

    public String memberJoin(Member member, HttpSession session, RedirectAttributes rttr) {
        log.info("memberJoin()");
        String msg = null;
        String view = null;
        Optional<Member> dbMem = mRepo.findById(member.getMid());


        if (dbMem.isEmpty()) { // 입력한 아이디가 있을 경우
            try {
                mRepo.save(member);
                msg = "가입 성공";
                view = "redirect:/";
            } catch (Exception e) {
                e.printStackTrace();
                msg = "가입 실패";
                view = "redirect:/";
            }
        } else {
            msg = "중복된 아이디입니다.";
            view = "redirect:/";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }


    public ModelAndView getBook(String bname) {
        log.info("getBook()");
        mv = new ModelAndView();
        String searchName = "%" + bname + "%";
        try {
            List<Book> gbook = (List<Book>) bRepo.findByBnameLike(searchName);
            mv.addObject("gbook", gbook);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mv.setViewName("booklist");
        return mv;
    }


    public String memberLogin(Member member, HttpSession session, RedirectAttributes rttr) {
        log.info("memberLogin()");
        String msg = null;
        String view = null;

        Optional<Member> mem = mRepo.findById(member.getMid());
        try {
            Member m2 = mem.get();
            if (m2.getMpwd().equals(member.getMpwd())) {
                msg = "로그인 성공";
                member = m2;
                session.setAttribute("mem", member);
                view = "redirect:/";
            } else {
                msg = "비밀번호 오류";
                view = "redirect:/";
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg = "로그인 불가능";
            view = "redirect:/";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    public String inputBook(Book book) {
        String view = null;
        try {
            bRepo.save(book);
            view = "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            view = "redirect:/";
        }
        return view;
    }

    public ModelAndView getBookList() {
        mv = new ModelAndView();
        mv.setViewName("bookcrud");

        List<Book> bList = new ArrayList<>();
        Iterable<Book> bIter = bRepo.findAll();

        for (Book b : bIter) {
            bList.add(b);
        }

        mv.addObject("blist", bList);
        return mv;
    }

    @Transactional
    public String deleteBook(Book bid) {
        String msg = null;
        try {
            bRepo.delete(bid);
            msg = "삭제 성공";
        } catch (Exception e) {
            e.printStackTrace();
            msg = "삭제 실패";
        }
        return msg;
    }


    @Transactional
    public String insertBoard(Board board, HttpSession session, RedirectAttributes rttr) {
        log.info("insertBoard()");
        String msg = null;
        String view = null;

        try {
            boRepo.save(board);
            log.info("bnum : " + board.getBnum());
//            fileUpload(files, session, board);

            view = "redirect:part";
            msg = "저장 성공";

        } catch (Exception e) {
            e.printStackTrace();
            view = "redirect:writeFrm";
            msg = "저장 실패";
        }
        rttr.addFlashAttribute("msg", msg);

        return view;
    }

    public ModelAndView getBoardList(Integer pageNum, HttpSession session) {
        log.info("getBoardList()");
        mv = new ModelAndView();

        if (pageNum == null) {
            pageNum = 1;
        }

        int listCnt = 5;    // 페이지당 보여질 게시글
        //페이징 조건 생성
        Pageable pb = PageRequest.of((pageNum - 1), listCnt,
                Sort.Direction.DESC, "bnum");

        Page<Board> result = boRepo.findByBnumGreaterThan(0L, pb);
        List<Board> bList = result.getContent();
        int totalPage = result.getTotalPages();
        String paging = getPaging(pageNum, totalPage);
        mv.addObject("bList", bList);
        mv.addObject("paging", paging);

        session.setAttribute("pageNum", pageNum);

        return mv;
    }

    //페이징 처리 메소드
    private String getPaging(Integer pageNum, int totalPage) {
        String pageHtml = null;
        int pageCnt = 2; //보여질 페이지 개수
        String listName = "?";

        PagingUtil paging = new PagingUtil(totalPage, pageNum, pageCnt, listName);

        pageHtml = paging.makePaging();

        return pageHtml;
    }

    public ModelAndView getBoard(long bnum) {
        log.info("getBoard()");
        mv = new ModelAndView();
        Board board = boRepo.findById(bnum).get();
        mv.addObject("board", board);

        return mv;
    }

    @Transactional
    public String boardUpdate(Board board, HttpSession session, RedirectAttributes rttr) {
        log.info("boardUpdate()");
        String msg = null;
        String view = null;

        try {
            boRepo.save(board);

            msg = "수정 성공";
            view = "redirect:detail?bnum=" + board.getBnum();
        } catch (Exception e) {
            e.printStackTrace();
            msg = "수정 실패";
            view = "redirect:updateFrm?bnum=" + board.getBnum();
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    @Transactional
    public String boardDelete(long bnum, HttpSession session, RedirectAttributes rttr) {
        log.info("boardDelete()");
        String msg = null;
        String view = null;

        try {
            boRepo.deleteById(bnum);
            msg = "삭제 성공";
            view = "redirect:part";
        } catch (Exception e) {
            msg = "삭제 실패";
            view = "redirect:detail?bnum=" + bnum;
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }


    private void fileUpload(List<MultipartFile> files, HttpSession session, Board board) {
        log.info("fileUpload()");
        String realPath = session.getServletContext().getRealPath("/");
        log.info("realPath: " + realPath);

        realPath += "upload/";
        File folder = new File(realPath);
        if (folder.isDirectory() == false) {
            folder.mkdir();
        }
        for (MultipartFile mf : files) {
            String orname = mf.getOriginalFilename();
            if (orname.equals("")) {
                return;
            }
            Board b = new Board();
        }
    }


    @Transactional
    public ModelAndView getTagList(String tag) {
        log.info("getList()");
        mv = new ModelAndView();

        if (tag != null) {
            try {
                List<Book> btaglist = bRepo.findByBtag(tag);
                mv.addObject("btaglist", btaglist);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            List<Book> btaglist = (List<Book>) bRepo.findAll();
            mv.addObject("btaglist", btaglist);
        }
        mv.setViewName("booklist");

        return mv;
    }


    // Book Rental Function
    @Transactional
    public String bookRent(Member member, Book book, RedirectAttributes rttr) {
        log.info("bookRent()");
        String msg = null;
        String view = null;

        if (member.getCount() < 5) {
            try {
                // 대여일 계산
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                String sdate = sdf.format(cal.getTime());

                book.setBsdate(sdate);

                // 반납일 계산
                cal.add(Calendar.DATE, 7);
                String edate = sdf.format(cal.getTime());
                book.setBedate(edate);

                // 현재 user의 대여수 증가
                member.setCount(member.getCount() + 1);

                mRepo.save(member);
                bRepo.save(book);
                msg = "대여 성공";
                view = "redirect:/";
            } catch (Exception e) {
                e.printStackTrace();
                msg = "대여 실패";
                view = "redirect:/";
            }
        } else {
            msg = "대여 가능 수를 초과하였습니다.";
            view = "redirect:/";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    // Book Return Function
    @Transactional
    public String bookReturn(Member member, Book book, RedirectAttributes rttr) {
        log.info("bookReturn()");
        String msg = null;
        String view = null;

        if (member.getCount() > 0) {
            try {
                book.setBlent(false);
                book.setBsdate("-");
                book.setBedate("-");

                member.setCount(member.getCount() - 1);
                bRepo.save(book);
                mRepo.save(member);
                msg = "반납 성공";
                view = "redirect:/";
            } catch (Exception e) {
                e.printStackTrace();
                msg = "반납 실패";
                view = "redirect:/";
            }
        } else {
            msg = "대여하신 책이 없습니다.";
            view = "redirect:/";
        }

        rttr.addFlashAttribute("msg", msg);
        return view;
    }


}
