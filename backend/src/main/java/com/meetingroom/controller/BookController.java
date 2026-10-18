package com.meetingroom.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meetingroom.common.PageUtils;
import com.meetingroom.common.Result;
import com.meetingroom.dto.AuditDTO;
import com.meetingroom.dto.BookAddDTO;
import com.meetingroom.service.BookService;
import com.meetingroom.vo.BookVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Tag(name = "预约模块")
@RestController
@RequestMapping("/book")
public class BookController {

    @Resource
    private BookService bookService;

    @Operation(summary = "提交预约")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody BookAddDTO dto) {
        bookService.add(dto);
        return Result.success();
    }

    @SaCheckRole("admin")
    @Operation(summary = "审核预约（管理员）")
    @PutMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody AuditDTO dto) {
        bookService.audit(dto);
        return Result.success();
    }

    @Operation(summary = "取消预约（本人）")
    @PutMapping("/cancel/{bookId}")
    public Result<Void> cancel(@PathVariable Long bookId) {
        bookService.cancel(bookId);
        return Result.success();
    }

    @Operation(summary = "查询本人预约记录")
    @GetMapping("/list")
    public Result<IPage<BookVO>> listMine(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(bookService.listMine(PageUtils.normalizePage(page), PageUtils.normalizeSize(size)));
    }

    @SaCheckRole("admin")
    @Operation(summary = "查询全部预约（管理员，支持筛选）")
    @GetMapping("/all")
    public Result<IPage<BookVO>> listAll(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) Long roomId,
                                         @RequestParam(required = false) String bookDate,
                                         @RequestParam(required = false) String auditStatus) {
        return Result.success(bookService.listAll(PageUtils.normalizePage(page), PageUtils.normalizeSize(size), roomId, bookDate, auditStatus));
    }

    @SaCheckRole("admin")
    @Operation(summary = "删除预约记录（管理员）")
    @DeleteMapping("/delete/{bookId}")
    public Result<Void> delete(@PathVariable Long bookId) {
        bookService.delete(bookId);
        return Result.success();
    }

    @Operation(summary = "查询某会议室某天已占用时段（所有登录用户）")
    @GetMapping("/occupiedSlots")
    public Result<java.util.List<String>> occupiedSlots(@RequestParam Long roomId,
                                                        @RequestParam String bookDate) {
        return Result.success(bookService.occupiedSlots(roomId, bookDate));
    }
}
