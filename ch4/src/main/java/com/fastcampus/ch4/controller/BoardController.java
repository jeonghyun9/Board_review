package com.fastcampus.ch4.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fastcampus.ch4.domain.BoardDto;
import com.fastcampus.ch4.domain.PageHandler;
import com.fastcampus.ch4.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/board")
public class BoardController {

    @Autowired
    BoardService boardService;

    @PostMapping("/modify")
    public String modify(BoardDto boardDto, Model m,HttpSession session
            , RedirectAttributes rattr) {
        String writer = (String) session.getAttribute("id");
        boardDto.setWriter(writer);
        try {
            int rowCnt = boardService.modify(boardDto);
            if (rowCnt != 1) {
                throw new Exception("modify failed");
            }
            rattr.addFlashAttribute("msg", "modify_OK");
            return "redirect:/board/list";
        } catch (Exception e) {
            e.printStackTrace();
            m.addAttribute(boardDto);
            m.addAttribute("msg", "modify_ERR");
            return "board";
        }
    }

    @PostMapping("/write")
    public String write(BoardDto boardDto, Model m,HttpSession session
                    , RedirectAttributes rattr) {
        String writer = (String) session.getAttribute("id");
        boardDto.setWriter(writer);
        try {
            int rowCnt = boardService.write(boardDto);
            if (rowCnt != 1) {
                throw new Exception("Write failed");
            }
            rattr.addFlashAttribute("msg", "WRT_OK");
            return "redirect:/board/list";
        } catch (Exception e) {
            e.printStackTrace();
            m.addAttribute(boardDto);
            m.addAttribute("msg", "WRT_ERR");
            return "board";
        }
    }

    @GetMapping("/write")
    public String write(Model m) {
        m.addAttribute("mode", "new");
        return "board"; // ????????? ????????? ??????. ????????? ????????? ?????? mode=new
    }

    @PostMapping("/remove")
    public String remove(Integer bno, Integer page, Integer pageSize, Model model,
                         HttpSession session, RedirectAttributes rattr) {
        String writer = (String) session.getAttribute("id");
        try {
            model.addAttribute(page);
            model.addAttribute(pageSize);
            int rowCnt = boardService.remove(bno, writer);
            if (rowCnt != 1)
//                model.addAttribute("msg", "DEL_OK");
//                return "redirect:/board/list";
                throw new Exception("board remove error");
//                model.addAttribute("msg", "DEL_ERR");
            rattr.addFlashAttribute("msg", "DEL_OK");

        } catch (Exception e) {
           e.printStackTrace();
//            model.addAttribute("msg", "DEL_OK");
            rattr.addFlashAttribute("msg", "DEL_ERR");
        }

        return "redirect:/board/list";
    }

    @GetMapping("/read")
    public String read(Integer bno, Model m, Integer page, Integer pageSize) {
        try {
            BoardDto boardDto = boardService.read(bno);
//            m.addAttribute(boardDto);
            m.addAttribute("boardDto", boardDto); //?????? ??????\
            m.addAttribute("page", page);
            m.addAttribute("pageSize", pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "board";
    }

    @GetMapping("/list")
    public String list(Integer page, Integer pageSize, Model model, HttpServletRequest request) {
        if(!loginCheck(request))
            return "redirect:/login/login?toURL="+request.getRequestURL();  // ???????????? ???????????? ????????? ???????????? ??????

        if (page == null) {
            page=1;
        }
        if (pageSize == null) {
            pageSize=10;
        }

        try {
            int totalCnt = boardService.getCount();
            PageHandler pageHandler = new PageHandler(totalCnt, page, pageSize);



            Map map = new HashMap<>();
            map.put("offset", (page - 1) * pageSize);
            map.put("pageSize", pageSize);

            List<BoardDto> list = boardService.getPage(map);
            model.addAttribute("list", list);
            model.addAttribute("ph", pageHandler);
            model.addAttribute("page", page);
            model.addAttribute("pageSize", pageSize);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "boardList"; // ???????????? ??? ????????????, ????????? ???????????? ??????
    }

    private boolean loginCheck(HttpServletRequest request) {
        // 1. ????????? ?????????
        HttpSession session = request.getSession();
        // 2. ????????? id??? ????????? ??????, ????????? true??? ??????
        return session.getAttribute("id")!=null;
    }
}