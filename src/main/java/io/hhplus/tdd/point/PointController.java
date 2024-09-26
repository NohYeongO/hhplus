package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    protected static final long MAX_LIMIT = 1000000; // 최대 금액으로 정의

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     *
     */
    @GetMapping("{id}")
    public ResponseEntity<UserPoint> point(@PathVariable long id) {
        return ResponseEntity.ok(pointService.selectPoint(id));
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     *
     */
    @GetMapping("{id}/histories")
    public ResponseEntity<List<PointHistory>> history(@PathVariable long id) {
        return ResponseEntity.ok(pointService.selectHistoryList(id));
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     *
     * 최대 충전 가능금액 1000000으로 제한
     */
    @PatchMapping("{id}/charge")
    public ResponseEntity<UserPoint> charge(@PathVariable long id, @RequestBody long amount) {
        if(amount > MAX_LIMIT){
            throw new IllegalArgumentException("최대 잔고 초과");
        } else if(amount < 0){
            throw new IllegalArgumentException("충전 불가 금액");
        }

        return ResponseEntity.ok(pointService.pointCharge(id, amount));
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     *
     */
    @PatchMapping("{id}/use")
    public ResponseEntity<UserPoint> use(@PathVariable long id, @RequestBody long amount) {
        if(amount > MAX_LIMIT) {
            throw new IllegalArgumentException("잔고 부족");
        } else if(amount < 0){
            throw new IllegalArgumentException("사용 불가 금액");
        }

        return ResponseEntity.ok(pointService.pointUse(id, amount));
    }
}
