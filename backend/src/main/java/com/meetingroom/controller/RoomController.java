package com.meetingroom.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meetingroom.common.PageUtils;
import com.meetingroom.common.Result;
import com.meetingroom.dto.RoomDTO;
import com.meetingroom.service.RoomService;
import com.meetingroom.vo.RoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Tag(name = "会议室模块")
@RestController
@RequestMapping("/room")
public class RoomController {

    @Resource
    private RoomService roomService;

    @Operation(summary = "分页查询会议室")
    @GetMapping("/list")
    public Result<IPage<RoomVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String location,
                                      @RequestParam(required = false) Integer capacity) {
        return Result.success(roomService.list(PageUtils.normalizePage(page), PageUtils.normalizeSize(size), location, capacity));
    }

    @Operation(summary = "获取会议室详情")
    @GetMapping("/{roomId}")
    public Result<RoomVO> detail(@PathVariable Long roomId) {
        return Result.success(roomService.detail(roomId));
    }

    @Operation(summary = "获取会议室可预约时段")
    @GetMapping("/getTimeSlot/{roomId}")
    public Result<List<String>> getTimeSlot(@PathVariable Long roomId) {
        return Result.success(roomService.getTimeSlot(roomId));
    }

    @SaCheckRole("admin")
    @Operation(summary = "新增会议室（管理员）")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody RoomDTO dto) {
        roomService.add(dto);
        return Result.success();
    }

    @SaCheckRole("admin")
    @Operation(summary = "修改会议室（管理员）")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody RoomDTO dto) {
        roomService.update(dto);
        return Result.success();
    }

    @SaCheckRole("admin")
    @Operation(summary = "删除会议室（管理员）")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return Result.success();
    }
}
